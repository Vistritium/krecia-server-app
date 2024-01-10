package krecia.maciejnowicki.com.mqtt

import akka.stream.alpakka.mqtt.MqttConnectionSettings
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

@Singleton
class MQTTConnection @Inject()(
) extends LazyLogging {

  def connectionSettings(clientId: String): MqttConnectionSettings = MqttConnectionSettings(
    "tcp://server.krecia.maciejnowicki.com:1883",
    clientId,
    new MemoryPersistence
  )
    .withAutomaticReconnect(true)

}
