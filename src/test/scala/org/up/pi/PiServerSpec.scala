package org.up.pi
import akka.actor.{Actor, Props, ActorRef, ActorRefFactory}
import spray.http.StatusCodes
import spray.httpx.SprayJsonSupport._
import spray.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import org.up.pi.actors._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
 
@RunWith(classOf[JUnitRunner])
class PiServerSpec extends Specification
                          with Specs2RouteTest {

  trait TestCreationSupport extends CreationSupport {
    def createChild(props: Props, name: String): ActorRef = system.actorOf(Props[FakeSensorActor])
    def getChild(name: String): Option[ActorRef] = None
  }

  val subject = new ReverseRoute with TestCreationSupport {
    implicit def actorRefFactory: ActorRefFactory = system
    implicit def executionContext = system.dispatcher
  }

  "calling /sensor" should {
    "Respond with a JSON response that contains a sensor value" in {
      Post("/sensor", SensorValueRequest("humidity")) ~> subject.sensorRoute ~> check {
        status === StatusCodes.OK
        val response = responseAs[SensorValueResponse]
        response.value must beEqualTo(200)
        response.stale must beFalse
      }
    }
  }
}

class FakeSensorActor extends Actor {

import SensorActor._

  def receive = {
    case SensorRequest("humidity", _) => sender ! SensorResult("humidity", Some(200), Low)
  }
}
