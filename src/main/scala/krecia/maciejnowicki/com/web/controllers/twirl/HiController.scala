package krecia.maciejnowicki.com.web.controllers.twirl

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import com.google.inject.{Inject, Singleton}
import krecia.maciejnowicki.com.detector.AlarmDetectorService
import krecia.maciejnowicki.com.web.{ControllerDeps, DiscoverableController, TwirlController}

@Singleton
@DiscoverableController
class HiController @Inject()(
  controllerDeps: ControllerDeps,
) extends TwirlController(controllerDeps) {
  override def route: Route = pathSingleSlash {
    get {
      complete(html.index.render())
    }
  }
}
