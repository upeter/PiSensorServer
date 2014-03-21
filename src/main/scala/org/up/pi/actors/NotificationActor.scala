package org.up.pi
package actors

import scala.concurrent.duration._
import scala.util.{ Success, Failure }
import akka.actor.{ Actor, Props }
import collection.immutable.Range
import org.joda.time._
import akka.actor.ActorLogging
import org.apache.commons.mail._
import scala.concurrent.duration.FiniteDuration
import org.apache.commons.mail.DefaultAuthenticator
import akka.util._
import java.util.concurrent.TimeUnit.SECONDS
import org.up.pi.config.Settings

object NotificationActor {
  def props = Props[NotificationActor]
  def name = "NotificationActor"

  sealed trait Notification {
    val subject: String
    val body: (String, Int) => xml.Elem
  }
  private def createNotificationBody(state: String, action: String)(host: String, port: Int): xml.Elem = {
    val linkToApp = s"http://$host:$port/app/index.html"
    <html><body>
            <p>Dear Plantkeeper,</p>
            <p>Your plant is <strong><a href={ linkToApp }>{ state }</a></strong>. { action }</p>
            <p>Many thanks in advance.</p>
            <p>Kind regards,</p>
            <p>Your Plant</p>
          </body></html>
  }

  case object ThirstyNotification extends Notification {
    val subject = "I'm thirsty!"
    val body = createNotificationBody("thirsty", "Please water me!")(_: String, _: Int)
  }

  case object DryingOutNotification extends Notification {
    val subject = "I'm drying out!"
    val body = createNotificationBody("drying out", "Please water me immediately!")(_: String, _: Int)
  }
  case object StaleNotification extends Notification {
    val subject = "Sensor value have become stale"
    val body = (h: String, p: Int) => <html><body><p>Check the sensor connection.</p></body></html>
  }
}

class NotificationActor extends Actor with ActorLogging with MailSender {
  import scala.util.control._
  import NotificationActor._
  import SensorActor._
  val settings = Settings(context.system)
  import settings._
  import Notification._
  import Mail._
  override val smtpConfig = SmtpConfig(ssl = Ssl, host = Host, user = User, password = Password)
  implicit val executionContext = context.dispatcher
  listenTo(classOf[SensorResult])

  var lastSentStatus: Map[Notification, DateTime] = Map().withDefault(notification => new DateTime().minusMinutes(2 * SendSameAfterMinutes))
  var lastSentHistory: (Notification, Int) = StaleNotification -> 0
  def receive: Receive = {
    case SensorResult(sensorName, sensorValue, status, true) => {
      sendNotification(StaleNotification)
    }
    case SensorResult(sensorName, sensorValue, Medium, _) =>
      sendNotification(ThirstyNotification)

    case SensorResult(sensorName, sensorValue, Low, _) =>
      sendNotification(DryingOutNotification)
  }

  private def sendNotification(notification: Notification) {
    def needsSending(notification: Notification): Boolean = {
      val lastSent = lastSentStatus(notification)
      val (lastNotification, subsequentReceivedCount) = lastSentHistory
      (lastNotification == notification) && subsequentReceivedCount > 10 &&
        Minutes.minutesBetween(lastSent, new DateTime()).getMinutes() > SendSameAfterMinutes
    }
    def nextSubsequentCount(notification: Notification): Int = {
      val (previousNotification, subsequentReceivedCount) = lastSentHistory
      if (notification == previousNotification) subsequentReceivedCount + 1 else 1
    }

    if (needsSending(notification)) {
      sendMail(notification.subject, notification.body(RestHost, RestPort))
      lastSentStatus = lastSentStatus + (notification -> new DateTime())
    }
    lastSentHistory = notification -> nextSubsequentCount(notification)

  }

  def sendMail(subject: String, body: xml.Elem): Unit = {
    Exception.handling(classOf[Throwable]) by (e => {
      log.error(e, "Error while sending sensor notification: {}", subject)
    }) apply {
      sendMail(Recipient, From, subject, body)
      log.info("Sent notification: {}", subject)
    }
  }

  private def listenTo(events: Class[_]*) = events foreach { c =>
    context.system.eventStream.subscribe(self, c)
  }

}
trait MailSender {

  val duration = FiniteDuration(10, SECONDS)
  val smtpConfig: SmtpConfig

  def sendMail(recipient: String, from: String, subject: String, body: xml.Elem): Unit = {
    val msg = EmailMessage(
      subject = subject,
      recipient = recipient,
      from = from,
      text = body.toString,
      html = body.toString,
      smtpConfig = smtpConfig)
    //    val msg = EmailMessage(
    //      subject = subject,
    //      recipient = "urs_peter@gmx.ch",
    //      from = "urs_peter@gmx.ch",
    //      text = body.toString,
    //      html = body.toString,
    //      smtpConfig = SmtpConfig(
    //        ssl = true,
    //        host = "mail.gmx.net",
    //        user = "urs_peter@gmx.ch",
    //        password = "takeyourstake"),
    //      retryOn = duration)
    sendEmailSync(msg)
  }

  private def sendEmailSync(emailMessage: EmailMessage) {
    // Create the email message
    val email = new HtmlEmail()
    email.setTLS(emailMessage.smtpConfig.tls)
    email.setSSL(emailMessage.smtpConfig.ssl)
    email.setSmtpPort(emailMessage.smtpConfig.port)
    email.setHostName(emailMessage.smtpConfig.host)
    email.setAuthenticator(new DefaultAuthenticator(
      emailMessage.smtpConfig.user,
      emailMessage.smtpConfig.password))
    email.setHtmlMsg(emailMessage.html)
      .setTextMsg(emailMessage.text)
      .addTo(emailMessage.recipient)
      .setFrom(emailMessage.from)
      .setSubject(emailMessage.subject)
      .send()
  }

  case class SmtpConfig(tls: Boolean = false,
    ssl: Boolean = false,
    port: Int = 25,
    host: String,
    user: String,
    password: String)

  case class EmailMessage(
    subject: String,
    recipient: String,
    from: String,
    text: String,
    html: String,
    smtpConfig: SmtpConfig,
    retryOn: FiniteDuration = duration,
    var deliveryAttempts: Int = 0)
}

