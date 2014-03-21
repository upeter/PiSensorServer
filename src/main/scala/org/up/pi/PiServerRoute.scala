package org.up
package pi

import actors._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }
import akka.actor.{ Props, ActorRef }
import akka.util.Timeout
import spray.routing._
import spray.httpx.SprayJsonSupport._
import spray.http.StatusCodes
import org.up.pi.actors.old.ReverseActor
import scala.concurrent.Future
class PiServerRoute extends HttpServiceActor
  with ReverseRoute
  with ActorContextCreationSupport {
  implicit def executionContext = context.dispatcher

  def receive = runRoute(sensorRoute)
}

trait ReverseRoute extends HttpService with CreationSupport {
  import SensorActor._
  implicit val timeout = Timeout(20 seconds)
  implicit def executionContext: ExecutionContext

  private val sensorActor = createChild(SensorActor.props, SensorActor.name)
  
  def sensorRoute: Route =
    path("sensor") {
      post {
        entity(as[SensorValueRequest]) { request =>
          val futureResponse = executeSensorCommand(request.name)
          onComplete(futureResponse) {
            case Success(SensorResult(name, value, status, stale)) => complete(SensorValueResponse(name, value.getOrElse(-1), status.value, stale))
            case Failure(e) => complete(StatusCodes.InternalServerError)
          }
        }
      }
    } ~
      pathPrefix("sensor" / Segment) { sensorName =>
        val futureResponse = executeSensorCommand(sensorName)
        onComplete(futureResponse) {
          case Success(SensorResult(name, value, status, stale)) => complete(SensorValueResponse(name, value.getOrElse(-1), status.value, stale))
          case Failure(e) => complete(StatusCodes.InternalServerError)
        }
      } ~
      //    path("") {
      //      getFromResource("index.html")
      //    } ~
      pathPrefix("app") {
        getFromResourceDirectory("app")
      } ~
      path("api" / "stocks") {
        complete("OK")
      }

  private def executeSensorCommand(sensorName: String): Future[SensorResult] = {
    implicit val timeout = Timeout(20 seconds)
    import akka.pattern.ask

    sensorActor.ask(SensorRequest(sensorName)).mapTo[SensorResult]
  }

}

