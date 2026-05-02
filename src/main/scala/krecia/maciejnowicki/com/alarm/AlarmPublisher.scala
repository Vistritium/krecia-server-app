package krecia.maciejnowicki.com.alarm

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.mqtt.scaladsl.MqttSink
import akka.stream.alpakka.mqtt.{MqttMessage, MqttQoS}
import akka.stream.scaladsl.{RestartSink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy, RestartSettings}
import akka.util.ByteString
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.AlarmState
import krecia.maciejnowicki.com.mqtt.MQTTConnection
import akka.actor.typed.scaladsl.adapter.*
import scala.concurrent.duration._

@Singleton
class AlarmPublisher @Inject()(
  MQTTConnection: MQTTConnection,
  actorSystem: ActorSystem
) extends LazyLogging {

  private implicit val implicitAs: ActorSystem = actorSystem

  private case class AlarmStateChange(
    state: AlarmState,
  )

  val publisher = actorSystem.spawn(alarmPublisherBehaviour, "alarmPublisher")

  private def alarmPublisherBehaviour: Behavior[AlarmDeviceState] = {

    val source = Source.actorRef[AlarmStateChange](
        completionMatcher = PartialFunction.empty,
        failureMatcher = PartialFunction.empty,
        2,
        OverflowStrategy.dropHead
      ).map { alarmStateChange =>
        MqttMessage("alarm_device", ByteString(alarmStateChange.state.toString)).withRetained(true)
      }
      .log("alarm-publisher")
      .to(
        RestartSink.withBackoff(RestartSettings(1.seconds, 1.minutes, 0.1)) { () =>
          MqttSink(MQTTConnection.connectionSettings("server-app-alarm"), MqttQoS.atLeastOnce)
        }
      )
      .run()

    Behaviors.receiveMessage { state =>
      logger.info(s"Publishing ${state.currentState}")
      source ! AlarmStateChange(state.currentState)
      Behaviors.same
    }

  }

}



