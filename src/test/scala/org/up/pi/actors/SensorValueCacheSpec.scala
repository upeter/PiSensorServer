package org.up.pi.actors

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope

@RunWith(classOf[JUnitRunner])
class SensorValueCacheSpec extends Specification {
  sequential

 import SensorValueCache._
  "SensorValueCache" should {
    val sensorName = "humidity"
    "cache sensor results" in {
      val sensorCache = createSensorCache
      val result = sensorCache.cache(sensorName, 2)
      result ==== sensorCache.get(sensorName)
    }
    "get default result if not cached" in {
      val sensorCache = createSensorCache
      val result = sensorCache.get(sensorName)
      result ==== EmptyResult(sensorName)
    }
    "calc outside range" in {
      val range = 10
      10 ==== nextOutsideRange(10, 20, range)
      21 ==== nextOutsideRange(10, 21, range)
    }
    "calc moving average" in {
      val r1 = calcMovingAvg(Seq.empty[Int], 10, 2)
      r1 ==== (10, Seq(10))
      val r2 = calcMovingAvg(r1._2, 30, 2)
      r2 ==== (20, Seq(30, 10))
      val r3 = calcMovingAvg(r2._2, 20, 2)
      r3 ==== (25, Seq(20, 30))

    }
    "only update if outside minimal deviation" in {
      val sensorCache = createSensorCache
      var samples = Seq.empty[Int]
      var sample = 100
      var result = sensorCache.cache(sensorName, sample)
      val (avg, s1) = calcMovingAvg(samples, sample, 10)
      val newVal = nextOutsideRange(sample, avg, 45)
      samples = s1
      result.value === Some(newVal)

      //      result = cache(sensorName, 2 + MinimalDeviation - 1)
      //      result.value === Some(2)
      //      result = cache(sensorName, 2 - MinimalDeviation + 1)
      //      result.value === Some(2)
      //      result = cache(sensorName, 2 + MinimalDeviation + 1)
      //      result.value === Some(2 + MinimalDeviation + 1)
    }
  }
 def createSensorCache = new SensorValueCache(5, 45, 10)
}