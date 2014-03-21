package org.up.pi.misc

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success
import akka.actor.ActorSystem
import akka.util.Timeout
import spray.http._
import spray.http._
import spray.http.HttpMethods._
import spray.json._
import spray.json.DefaultJsonProtocol._
import spray.http.HttpHeaders.Cookie
import akka.util.Timeout.durationToTimeout
import spray.client.pipelining.Get
import spray.client.pipelining.Post
import spray.client.pipelining.WithTransformerConcatenation
import spray.client.pipelining.addHeader
import spray.client.pipelining.sendReceive
import spray.http.ContentType.apply
import spray.http.HttpHeaders.Location
import spray.http.Uri.apply
trait Defaults {
  implicit val timeout: Timeout = 2 seconds

}
object SprayClientSample extends App with Defaults {
  import spray.http._
  import spray.client.pipelining._

  implicit val system = ActorSystem()
  import system.dispatcher // execution context for futures

  //val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  val pipeline: HttpRequest => Future[HttpResponse] = {
    sendReceive
    //~> decode(Gzip)
  }

  val pipeline2: HttpRequest => Future[HttpResponse] = (
    sendReceive)

  val url = "https://login.vodafone.nl/signon?provider=myvodafone"
  val raw = "loginerrorurl=My_Vodafone%2Fgratis_sms_versturen&assertionconsumerurl=My_Vodafone%2Fgratis_sms_versturen&username=0610905204&password=Success10"

  def request(url: String, raw: String) = HttpRequest(method = HttpMethods.POST, uri = url,
    entity = HttpEntity(MediaTypes.`application/x-www-form-urlencoded`, raw))

  //  val redirectConf = system.settings.config withValue
  //    ("spray.can.host-connector.max-redirects", ConfigValueFactory.fromAnyRef(12))
  //
  //  val hostname = "my.vodafone.nl"
  //  val port = 443
  //  val setup = Http.HostConnectorSetup(hostname, port, settings = Some(HostConnectorSettings(redirectConf)), sslEncryption = true)
  //val res = (IO(Http) ? request).mapTo[HttpResponse]

  //    Await.ready(res, 5 seconds)
  //
  //    res.onComplete {
  //    case Success(res) => {
  //      println(res)
  //    }
  //    case e => println(s"unkown: $e")
  //  }

  val url2 = "https://my.vodafone.nl/My_Vodafone/gratis_sms_versturen?"
  val raw2 = "_fs=es_My+Vodafone_gratis+sms+versturen&_fp=GratisSMSVersturen&_st=&phoneNumber=0610905204&body=test3"
  def getCookie(name: String): HttpHeader => Option[HttpCookie] = {
    case Cookie(cookies) => cookies.find(_.name == name)
  }
  import HttpHeaders.Cookie._
  import HttpHeaders._
  import HttpHeader._

  def next(resp: HttpResponse): Future[HttpResponse] = {
    val cookies = resp.headers.collect { case `Set-Cookie`(hc) => hc }
    val location = resp.headers.collect { case `Location`(hc) => hc }
    println(s"forward to without cookies: $location")
    //val pipeline = addHeader(Cookie(cookies)) ~> sendReceive
    val pipeline = sendReceive
    pipeline(Get(location.head))
  }
  
   def nextWithCookies(resp: HttpResponse): Future[HttpResponse] = {
    val cookies = resp.headers.collect { case `Set-Cookie`(hc) => hc }
    val location = resp.headers.collect { case `Location`(hc) => hc }
    println(s"forward to with cookies: $location")
    println(cookies)
    val pipeline = addHeader(Cookie(cookies)) ~> sendReceive
    pipeline(Get(location.head))
  }

def cookiePipeline(resp: HttpResponse):HttpRequest => Future[HttpResponse] = {
    val cookie = resp.headers.collect { case `Set-Cookie`(hc) => hc }
   addHeader(Cookie(cookie)) ~> sendReceive
  }

    val res: Future[HttpResponse] = pipeline2(request(url, raw))

  val finalRes = for {
    r0 <- pipeline2(request(url, raw))
    r1 <- next(r0)
    r2 <- nextWithCookies(r1)
    r3 <- nextWithCookies(r2)
    //r4 <- next(r3)
   // r4 <- cookiePipeline(r3)(request(url2, raw2))

    //    r4 <- next(r3)
    //    r5 <- next(r4)
    //    r6 <- next(r5)
    //    r7 <- next(r6)
  } yield r3

  Await.ready(finalRes, 5 seconds)

  finalRes.onComplete {
    case Success(res) => {
      println(res.entity.data.asString)
    }
    case e => println(s"unkown: $e")
  }

  //  res.onComplete {
  //    case Success(res) => {
  //      println(res.headers)
  //      val cookie = res.headers.collect{ case `Set-Cookie`(hc) => hc}
  //     val location = res.headers.collect{ case `Location`(hc) => hc}
  //      println(s"$cookie $location")
  //      println(res)
  //    }
  //    case e => println(s"unkown: $e")
  //  }
  system.shutdown()

  def stuff() {
    val response: Future[HttpResponse] = pipeline(Get("http://spray.io/"))
    val response0: Future[HttpResponse] = pipeline(Post("http://spray.io/"))

    //val r = Await.ready(response, 3 seconds)

    val res = Future.sequence(Seq(response, response0)).map(r => r.seq.map(r => println(r.entity.data.asString)))
    Await.ready(res, 5 seconds)
    val jsonAst = List(1, 2, 3).toJson
    println(jsonAst.prettyPrint)

  }

}