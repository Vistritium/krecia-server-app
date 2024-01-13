package krecia.maciejnowicki.com.mqtt

import akka.stream.alpakka.mqtt.MqttConnectionSettings
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

@Singleton
class MQTTConnection @Inject()(
  config: Config,
) extends LazyLogging {

  private val prefix: String = config.getString("mqttClientPrefix")
  def connectionSettings(clientId: String): MqttConnectionSettings = MqttConnectionSettings(
    "tcp://server.krecia.maciejnowicki.com:1883",
    prefix + clientId,
    new MemoryPersistence
  )
    .withAutomaticReconnect(true)

}
