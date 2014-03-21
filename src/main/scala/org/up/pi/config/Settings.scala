package org.up.pi.config
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.actor.ExtendedActorSystem
import scala.concurrent.duration.Duration
import com.typesafe.config.Config
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class SettingsImpl(config: Config) extends Extension {

  val RestHost: String = config.getString("pi-server.rest.host")
  val RestPort: Int = config.getInt("pi-server.rest.port")

  object Notification {
    val SendSameAfterMinutes: Int =
      config.getInt("pi-server.notification.send.same.notification.after.minutes")

    object Mail {
      val Recipient: String =
        config.getString("pi-server.notification.mail.recipient")
      val From: String =
        config.getString("pi-server.notification.mail.from")
      val Ssl: Boolean =
        config.getBoolean("pi-server.notification.mail.ssl")
      val Host: String =
        config.getString("pi-server.notification.mail.host")
      val User: String =
        config.getString("pi-server.notification.mail.user")
      val Password: String =
        config.getString("pi-server.notification.mail.password")
    }
  }

  object Sensor {
    val SerialPort: String =
        config.getString("pi-server.sensor.serial.port")
     val UpdateInterval: FiniteDuration=
      FiniteDuration(config.getMilliseconds("pi-server.sensor.update.interval"), TimeUnit.MILLISECONDS)
    
    val SampleConsideredStaleAfterHours: Int =
      config.getInt("pi-server.sensor.sample.considered.stale.after.hours")
    val MandatoryDeviationBetweenSamplesForChange: Int =
      config.getInt("pi-server.sensor.mandatory.deviation.between.samples.for.change")
    val MaxSamplesForMovingAverage: Int =
      config.getInt("pi-server.sensor.max.samples.for.moving.average")
  }

override def toString = config.getConfig("pi-server").toString
}


object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {

  override def lookup = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)
}


