package org.up.pi
package actors

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.actor.{ Actor, Props }
import collection.immutable.Range
import org.joda.time._
import akka.actor.ActorLogging
import config.Settings
object SensorActor {
  def props = Props[SensorActor]
  def name = "sensorActor"

  case class ScheduledSensorRequest(name: String, param: Option[String] = None)
  case class SensorRequest(name: String, param: Option[String] = None)
  case class SensorResult(name: String, value: Option[Int], status: Status, stale: Boolean = false)

  trait SensorMetaData {
    val name: String
    val statusMapping: Map[Range, Status]
    def mapTo(value: Int): Status = statusMapping.find { case (k, n) => k.contains(value) }.map(_._2).getOrElse(Unkown)
  }

  object SensorMetaData {
    private val sensorMetaData = Map(HumidityMetaData.toTuple).withDefaultValue((value: Int) => Unkown)
    def mapToStatus(name: String): Int => Status = sensorMetaData(name)
  }

  case object HumidityMetaData extends SensorMetaData {
    val name: String = "humidity"
    val statusMapping: Map[Range, Status] = Map(0.to(249) -> Low, 250.to(449) -> Medium, 450.to(749) -> High, 750.to(950) -> TooHigh)
    val toTuple: (String, Int => Status) = name -> mapTo
  }

}

class SensorActor extends Actor with ActorLogging with Things {
  import SensorActor._
  import SensorMetaData._
  import scala.util.control._
  import Actor._
  val settings = Settings(context.system)
  import settings._
  import Sensor._

  val scheduler = context.system.scheduler
  val eventbus = context.system.eventStream
  val sensorValueCache = new SensorValueCache(SampleConsideredStaleAfterHours, MandatoryDeviationBetweenSamplesForChange, MaxSamplesForMovingAverage)
  override val serialPort = SerialPort
  implicit val executionContext = context.dispatcher

  override def preStart = self ! ScheduledSensorRequest(HumidityMetaData.name)

  def receive: Receive = {
    case ScheduledSensorRequest(sensorName, param) => {
      Exception.handling(classOf[Throwable]) by (e => {
        log.error(e, "error occured while retrieving sensor value for {}", sensorName)
      }) apply {
        val rawValue = executeCommand(sensorName, param)
        val sensorValue = rawValue.toInt
        val result = sensorValueCache.cache(sensorName, sensorValue)
        eventbus.publish(result)
      }
      scheduler.scheduleOnce(UpdateInterval)(self ! ScheduledSensorRequest(sensorName, param))
    }
    case SensorRequest(sensorName, param) => {
      sender ! sensorValueCache.get(sensorName)
    }
  }

}

class SensorValueCache(val samplesConsideredStaleAfterHours: Int, val mandatoryDeviationBetweenSamples: Int, val maxSamplesForMovingAverage: Int) {
  import SensorActor._
  import SensorMetaData._
  import SensorValueCache._

  val DefaultEntry = (sensorName: String) => (EmptyResult(sensorName), new DateTime().minusHours(samplesConsideredStaleAfterHours), Seq.empty[Int])
  private var sensorValueCache: Map[String, (SensorResult, DateTime, Seq[Int])] = Map().withDefault(DefaultEntry)

  def clear() = sensorValueCache = sensorValueCache.empty

  def cache(sensorName: String, value: Int): SensorResult = {
    val (cachedResult, lastUpdate, samples) = sensorValueCache(sensorName)
    val (newAvg, newSamples) = calcMovingAvg(samples, value, maxSamplesForMovingAverage)
    val newValue = cachedResult.value.map(existing => nextOutsideRange(existing, newAvg, mandatoryDeviationBetweenSamples)).getOrElse(newAvg)
    val result = SensorResult(sensorName, Some(newValue), mapToStatus(sensorName)(newValue))
    sensorValueCache = sensorValueCache + (sensorName -> (result, new DateTime(), newSamples))
    result
  }

  def get(sensorName: String): SensorResult = sensorValueCache.get(sensorName)
    .map { case (cachedResult, lastUpdate, _) => cachedResult.copy(stale = isStale(lastUpdate)) }
    .getOrElse(EmptyResult(sensorName))

  private def isStale(date: DateTime): Boolean = Hours.hoursBetween(new DateTime(), date).getHours() > samplesConsideredStaleAfterHours

}

object SensorValueCache {
  import SensorActor._
  import SensorMetaData._
  val EmptyResult = (sensorName: String) => SensorResult(sensorName, None, Unkown, true)
  
  def nextOutsideRange(existing: Int, newSample: Int, deviation: Int) = {
    if (newSample > existing + deviation || newSample < existing - deviation) newSample else existing
  }

  def calcMovingAvg(history: Seq[Int], newSample: Int, maxSamples: Int): (Int, Seq[Int]) = {
    val samples = newSample +: history
    val rangeSamples = samples.slice(0, maxSamples)
    rangeSamples.sum / rangeSamples.size -> rangeSamples
  }
}

trait FakeThings extends Things {
  val end = 950
  override def executeCommand(sensorName: String, param: Option[String] = None): String = (util.Random.nextInt(end)).toString
}

trait Things {
  import org.things.Things.things
  val serialPort: String

  def executeCommand(sensorName: String, param: Option[String] = None): String = {
    try {
    things.execute(serialPort, sensorName, param.getOrElse(null));
    } catch {
      case e:Exception => {
        things.close();
        throw e
      }
      
    }
  }

}