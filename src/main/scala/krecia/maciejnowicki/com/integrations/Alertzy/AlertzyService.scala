package krecia.maciejnowicki.com.integrations.Alertzy

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.HttpExt
import org.apache.pekko.http.scaladsl.model.*
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.integrations.CriticalNotificationIntegration

import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

@Singleton
class AlertzyService @Inject()(
  config: Config,
  httpExt: HttpExt,
  ec: ExecutionContext,
  as: ActorSystem,
) extends CriticalNotificationIntegration with LazyLogging {

  private implicit val implicitEc: ExecutionContext = ec
  private implicit val implicitAs: ActorSystem = as

  private val accountIds = config.getStringList("alertzy.accountIds").asScala

  override def send(): Future[Unit] = {

    for {
      response <- httpExt.singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = Uri("https://alertzy.app/send"),
        entity = FormData(Map(
          "accountKey" -> accountIds.mkString("_"),
          "title" -> "Krecia alarm",
          "message" -> "Krecia alarm",
          "priority" -> "2"
        )).toEntity
      ))
      strict <- response.entity.toStrict(10.seconds)

    } yield {
      logger.info(s"${response.status.value} - ${strict.data.utf8String}")
      ()
    }

  }
}
