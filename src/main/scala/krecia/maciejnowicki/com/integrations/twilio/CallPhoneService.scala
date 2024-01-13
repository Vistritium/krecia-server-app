package krecia.maciejnowicki.com.integrations.twilio

import com.google.inject.{Inject, Singleton}
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Call
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import java.net.URI
import scala.jdk.FutureConverters._

@Singleton
class CallPhoneService @Inject()(
  config: Config,
) extends LazyLogging {

  private val sid: String = config.getString("twilio.sid")
  private val secret: String = config.getString("twilio.secret")

  Twilio.init(sid, secret);

  private var calledAlready = false

  def call(): Unit = this.synchronized {
    logger.info("Calling!!!")
    if (!calledAlready) {
      calledAlready = true

      val call = Call.creator(
        new com.twilio.`type`.PhoneNumber("+48514642718"),
        new com.twilio.`type`.PhoneNumber("+48514642718"),
        URI.create("http://demo.twilio.com/docs/voice.xml")
      ).create();


    } else {
      logger.error("Called already")
    }

  }


}
