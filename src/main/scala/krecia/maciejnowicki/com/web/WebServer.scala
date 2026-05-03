package krecia.maciejnowicki.com.web

import java.lang.reflect.Modifier

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import com.google.inject.{Inject, Injector, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.reflections.Reflections
import org.reflections.scanners.{SubTypesScanner, TypeAnnotationsScanner}
import org.reflections.util.{ClasspathHelper, ConfigurationBuilder}
import org.apache.pekko.http.scaladsl.server.Directives._

import collection.JavaConverters._
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Singleton
class WebServer @Inject()(
  executionContext: ExecutionContext,
  actorSystem: ActorSystem,
  config: Config,
  injector: Injector
) extends LazyLogging {

  private implicit val implicitEc: ExecutionContext = executionContext
  private implicit val implicitAs: ActorSystem = actorSystem

  private val controllers: List[Controller] = {
    val reflections = new Reflections(new ConfigurationBuilder()
      .setUrls(ClasspathHelper.forPackage(getClass.getPackage.getName))
      .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner))
    val set = reflections.getTypesAnnotatedWith(classOf[DiscoverableController]).asScala
      .filterNot(c => Modifier.isAbstract(c.getModifiers))
    logger.info(s"Found following controllers: ${set.mkString("\n", "\n", "")}")
    set.map(c => injector.getInstance(c).asInstanceOf[Controller]).toList
  }

  require(controllers.nonEmpty)
  private val route = controllers.map(_.route).reduce(_ ~ _)

  private val bindingFuture = {
    val port: Int = config.getInt("web.port")
    val boundInterface = "0.0.0.0"
    val futureBinding = Http().bindAndHandle(route, boundInterface, port)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info(s"Server bound to ${address.getHostString}:${address.getPort}")
      case Failure(exception) =>
        logger.error("Failed to bind HTTP server", exception)
        actorSystem.terminate()
    }(executionContext)

    futureBinding
  }
}
