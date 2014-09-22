package org.up.pi

import spray.json.DefaultJsonProtocol

case class ReverseRequest(value: String)

object ReverseRequest extends DefaultJsonProtocol {
  implicit val format = jsonFormat1(ReverseRequest.apply)
}
case class ReverseResponse(value: String, isPalindrome: Boolean = false) {
}

object ReverseResponse extends DefaultJsonProtocol {
  implicit val format = jsonFormat2(ReverseResponse.apply)
}

case class SensorValueRequest(name: String)

object SensorValueRequest extends DefaultJsonProtocol {
  implicit val format = jsonFormat1(SensorValueRequest.apply)
}
case class SensorValueResponse(name: String, value: Int,  status:String, stale: Boolean)

object SensorValueResponse extends DefaultJsonProtocol {
  implicit val format = jsonFormat4(SensorValueResponse.apply)
}



