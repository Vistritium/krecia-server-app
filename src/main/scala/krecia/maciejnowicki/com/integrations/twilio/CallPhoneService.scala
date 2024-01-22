package krecia.maciejnowicki.com.integrations.twilio

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import com.google.inject.{Inject, Singleton}
import com.twilio.http.TwilioRestClient
import com.twilio.rest.api.v2010.account.Call
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.integrations.CriticalNotificationIntegration

import java.net.URI
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success}

@Singleton
class CallPhoneService @Inject()(
  config: Config,
  ec: ExecutionContext,
  ak: ActorSystem,
) extends CriticalNotificationIntegration with LazyLogging {

  private implicit val implicitAs: ActorSystem = ak

  private case class TwilioConfig(
    name: String,
    phone: String,
    sid: String,
    secret: String,
  )

  private implicit val implicitEc: ExecutionContext = ec

  private val enabled = config.getStringList("twilio.enabled").asScala.toList

  private val configs = {
    val configs = config.getConfigList("twilio.accounts").asScala.map { conf =>
      val config = TwilioConfig(
        conf.getString("name"),
        conf.getString("phone"),
        conf.getString("sid"),
        conf.getString("secret"),
      )
      require(config.sid.nonEmpty, s"${config.name} sid cannot be empty")
      require(config.secret.nonEmpty, s"${config.name} secret cannot be empty")
      config
    }.filter(e => enabled.contains(e.name)).toList
    logger.info(s"Configured with ${configs.map(_.name).mkString(",")} twilio accounts")
    configs
  }

  def call(): Future[Done] = this.synchronized {


    Source(configs).runWith(Sink.foreachAsync(2) { config =>
      val f = Future {
        logger.info(s"Calling ${config.name} !!!")

        val client = new TwilioRestClient.Builder(config.sid, config.secret).build()


        val call = Call.creator(
          new com.twilio.`type`.PhoneNumber(config.phone),
          new com.twilio.`type`.PhoneNumber(config.phone),
          URI.create("http://demo.twilio.com/docs/voice.xml")
        ).create(client);

      }

      f.onComplete {
        case Failure(exception) => logger.error(s"failed to call ${config.name}", exception)
        case Success(value) => logger.info(s"Successfully called ${config.name}")
      }

      f
    })

  }

  override def send(): Future[Unit] = call().map(_ => ())

}
