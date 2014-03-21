package org.up.pi.misc

import org.apache.commons.mail.HtmlEmail
import scala.concurrent.duration.FiniteDuration
import org.apache.commons.mail.DefaultAuthenticator
import akka.util._
import java.util.concurrent.TimeUnit._

object SendMailSample extends App {
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

  val text = <html><body><h1>Hello</h1></body></html>
  val duration = FiniteDuration(10, SECONDS)

  val msg2 = EmailMessage(
    subject = "test subject",
    recipient = "urs_peter@gmx.ch",
    from = "urs_peter@gmx.ch",
    text = "Text",
    html = text.toString,
    smtpConfig = SmtpConfig(
      ssl = true,
      host = "mail.gmx.net",
      user = "urs_peter@gmx.ch",
      password = "takeyourstake"),
    retryOn = duration)
    
     val msg = EmailMessage(
    subject = "test subject",
    recipient = "urs_peter@localhost",
    from = "urs_peter@gmx.ch",
    text = "Text",
    html = text.toString,
    smtpConfig = SmtpConfig(
      ssl = false,
      host = "127.0.0.1",
      user = "urs_peter@localhost",
      password = "takeyourstake"),
    retryOn = duration)
  sendEmailSync(msg)

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
  retryOn: FiniteDuration,
  var deliveryAttempts: Int = 0)
 