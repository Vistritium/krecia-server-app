package krecia.maciejnowicki.com.notification

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.integrations.twilio.CallPhoneService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.adapter.*
import akka.stream.RestartSettings
import akka.stream.alpakka.mqtt.{MqttQoS, MqttSubscriptions}
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.{RestartSource, Source}
import com.typesafe.config.Config
import krecia.maciejnowicki.com.integrations.CriticalNotificationIntegration
import krecia.maciejnowicki.com.mqtt.MQTTConnection

import scala.concurrent.duration.*

@Singleton
class NotificationService @Inject()(
  criticalNotificationIntegration: CriticalNotificationIntegration,
  ec: ExecutionContext,
  actorSystem: ActorSystem,
  config: Config,
  MQTTConnection: MQTTConnection
) extends LazyLogging {

  private implicit val implicitEc: ExecutionContext = ec
  private implicit val implicitAs: ActorSystem = actorSystem

  private val enabled = config.getBoolean("notification.enabled")

  private case class State(
    enabled: Boolean
  )

  private def source: Source[String, NotUsed] = {
    val settings = RestartSettings(
      minBackoff = 3.seconds,
      maxBackoff = 30.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
    )

    RestartSource.withBackoff(settings) { () =>
      MqttSource.atMostOnce(
        MQTTConnection.connectionSettings("server-app-notification"),
        MqttSubscriptions(Map("ha/alarm" -> MqttQoS.ExactlyOnce)),
        bufferSize = 8
      ).map(msg => {
        msg.payload.utf8String
      })
    }

  }

  val criticalNotification: ActorRef[CriticalNotificationServiceCommand] = {
    val alarmDetector = actorSystem.spawn(behaviour(State(enabled)), "criticalNotification")
    source.runForeach(s => {
      logger.info(s"Received ${s} from mqtt")
      if (s == "ON") {
        alarmDetector.tell(CriticalNotificationServiceCommand.Send)
      } else {
        logger.error("Unknown message - " + s)
      }
    })

    alarmDetector
  }

  private def behaviour(state: State): Behavior[CriticalNotificationServiceCommand] = Behaviors.receiveMessage {
    case CriticalNotificationServiceCommand.Send => {
      if (state.enabled) {
        logger.info("Notification service enabled and sending critical notification")
        criticalNotificationIntegration.send().onComplete {
          case Failure(exception) => logger.error("Couldn't invoke critical notification", exception)
          case Success(value) =>
        }

//        behaviour(state.copy(enabled = false))
        Behaviors.same
      } else {
        logger.info("notification was already sent")
        Behaviors.same
      }
    }
  }

}

sealed trait CriticalNotificationServiceCommand

object CriticalNotificationServiceCommand {
  case object Send extends CriticalNotificationServiceCommand
}
