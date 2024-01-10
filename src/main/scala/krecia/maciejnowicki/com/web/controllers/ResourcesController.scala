package krecia.maciejnowicki.com.web.controllers

import akka.http.scaladsl.server.Route
import com.google.inject.{Inject, Singleton}
import krecia.maciejnowicki.com.web.{Controller, ControllerDeps, DiscoverableController}
import com.typesafe.scalalogging.LazyLogging
import akka.http.scaladsl.model.headers.*
import akka.http.scaladsl.model.headers.CacheDirectives.*
import akka.http.scaladsl.server.Directives.*

import scala.concurrent.duration.*

@Singleton
@DiscoverableController
class ResourcesController @Inject()(
  controllerDeps: ControllerDeps,
) extends Controller(controllerDeps) with LazyLogging {
  override def route: Route =
    respondWithHeader(`Cache-Control`(`public`, `max-age`(1.hour.toSeconds))) {
      pathPrefix("web") {
        getFromResourceDirectory("web", getClass.getClassLoader)
      } ~ get {
        pathPrefix("favicon.ico") {
          getFromResource("web/favicon.ico")
        }
      }
    }
}
