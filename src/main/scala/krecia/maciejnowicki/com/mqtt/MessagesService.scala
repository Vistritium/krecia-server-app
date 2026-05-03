package krecia.maciejnowicki.com.mqtt

import org.apache.pekko.{Done, NotUsed}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.RestartSettings
import org.apache.pekko.stream.connectors.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import org.apache.pekko.stream.connectors.mqtt.scaladsl.MqttSource
import org.apache.pekko.stream.scaladsl.{RestartSource, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.duration.*
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class MessagesService @Inject()(
  as: ActorSystem,
  ec: ExecutionContext,
  mapper: ObjectMapper,
  MQTTConnection: MQTTConnection,
) extends LazyLogging {

  private implicit val implicitEc: ExecutionContext = ec
  private implicit val implicitAs: ActorSystem = as



  def source: Source[BinaryStateEvent, NotUsed] = {
    val settings = RestartSettings(
      minBackoff = 3.seconds,
      maxBackoff = 60.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
    )

    RestartSource.withBackoff(settings) { () =>
      logger.info("Creating MQTT connection")
      MqttSource.atMostOnce(
        MQTTConnection.connectionSettings("server-app"),
        MqttSubscriptions(Map(
          "ha/binary_sensor/#" -> MqttQoS.ExactlyOnce,
          "ha/frigate_notification/#" -> MqttQoS.ExactlyOnce,
        )),
        bufferSize = 8
      ).map(msg => {
        val payload = msg.payload.utf8String
        logger.info(s"Reading ${payload}")
        mapper.readValue(payload, classOf[BinaryStateEvent])
      })
    }
  }

  def go(): Unit = {
    source.runForeach(
      msg => logger.info(s"Received message: ${msg}")
    ).onComplete {
      case Failure(exception) => logger.error("Error", exception)
      case Success(value) => logger.info(s"Finished??? ${value}")
    }
  }


}