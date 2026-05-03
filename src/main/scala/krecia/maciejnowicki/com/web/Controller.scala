package krecia.maciejnowicki.com.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.{Directive1, Route}
import akka.util.Timeout
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.*
import scala.util.{Failure, Success, Try}


case class ControllerDeps(
  mapper: ObjectMapper
)
@DiscoverableController
abstract class Controller(controllerDeps: ControllerDeps) extends LazyLogging {

  protected implicit val timeout: Timeout = Timeout(1.hour)

  def route: Route

  def handleFutureError[T](dir: Directive1[Try[T]])(next: T => Route): Route = {
    dir {
      case Failure(exception) => {
        logger.debug("futureError", exception)
        complete(HttpResponse(status = StatusCodes.InternalServerError, entity = s"${exception.getMessage}"))
      }
      case Success(value) => next(value)
    }
  }

  def completeJson(obj: Any) = complete(HttpEntity(ContentTypes.`application/json`, controllerDeps.mapper.writeValueAsString(obj)))

}
