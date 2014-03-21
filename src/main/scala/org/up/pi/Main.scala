package org.up
package pi


import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import spray.can.Http
import spray.can.Http.Bind
import actors._
import org.up.pi.config.Settings
object Main extends App {

  implicit val system = ActorSystem("pi-server")
  val settings = Settings(system)
  println(settings)
  
  system.actorOf(NotificationActor.props, NotificationActor.name)

  val piServer = system.actorOf(Props[PiServerRoute], "pi-server-actor")

  IO(Http) ! Bind(listener= piServer, interface = "0.0.0.0", port=settings.RestPort)
}

