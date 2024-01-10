package krecia.maciejnowicki.com.mqtt

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.RestartSettings
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.{RestartSource, Source}
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
      maxBackoff = 30.seconds,
      randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
    ).withMaxRestarts(30, 5.minutes) // limits the amount of restarts to 20 within 5 minutes

    RestartSource.withBackoff(settings) { () =>
      MqttSource.atMostOnce(
        MQTTConnection.connectionSettings("server-app"),
        MqttSubscriptions(Map("ha/binary_sensor/#" -> MqttQoS.ExactlyOnce)),
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