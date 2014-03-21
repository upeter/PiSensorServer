package org.up.pi

sealed trait Status {
  val value:String
}
case object Low extends Status {val value = "Low"} 
case object  Medium extends Status{ val value = "Medium"}
case object  High extends Status{ val value = "High"}
case object  TooHigh extends Status{ val value = "TooHigh"}
case object  Unkown extends Status{ val value = "Unkown"}
