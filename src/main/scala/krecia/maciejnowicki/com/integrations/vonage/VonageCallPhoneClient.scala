package krecia.maciejnowicki.com.integrations.vonage

import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import com.vonage.client.VonageClient
import com.vonage.client.voice.Call
import com.vonage.client.voice.ncco.{Action, TalkAction}
import org.apache.commons.io.IOUtils

import scala.jdk.CollectionConverters.*

@Singleton
class VonageCallPhoneClient @Inject()(
) extends LazyLogging {

  def call(): Unit = {

    val privateKeyContents = IOUtils.toString(getClass.getResourceAsStream("/vonage/key"))
    require(privateKeyContents != null, "privateKeyContents cannot be null")

    val client = VonageClient.builder()
      .applicationId("1d8fe2e8-07bb-49c0-8453-7319062735fa")
      .privateKeyContents(privateKeyContents)
      .build()

    val action = (TalkAction.builder("Krecia alarm").build()): Action
    try {
      val event = client.getVoiceClient
        .createCall(new Call(
          "48514642718",
          "48514642718",
          Seq(action).asJava
        ))
      logger.info(s"status: ${event.getStatus}")

      val uuid = event.getUuid

      val info = client.getVoiceClient.getCallDetails(uuid)

      logger.info(info.toString)

      Thread.sleep(1000 * 3)

      val info2 = client.getVoiceClient.getCallDetails(uuid)

      logger.info(info2.toString)

    } catch {
      case e: Exception => {
        logger.error("Failed to call vonage", e)
      }
    }




  }

}
