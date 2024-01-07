package krecia.maciejnowicki.com.mqtt

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.mqtt.{MqttConnectionSettings, MqttMessage, MqttQoS, MqttSubscriptions}
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.scaladsl.Source
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MessagesService @Inject()(
  as: ActorSystem
) extends LazyLogging {

  private implicit val implicitAs: ActorSystem = as

  private val connectionSettings = MqttConnectionSettings(
    "tcp://server.krecia.maciejnowicki.com:1885",
    "server-app",
    new MemoryPersistence
  )

  def source: Source[MqttMessage, Future[Done]] = MqttSource.atMostOnce(
    connectionSettings.withClientId(clientId = "server-app"),
    MqttSubscriptions(Map("ha/binary_sensor/+" -> MqttQoS.ExactlyOnce)),
    bufferSize = 8
  )

  def go(): Unit = {
    source.runForeach(
      msg => logger.info(s"Received message: ${msg.payload.utf8String}")
    )
  }


}