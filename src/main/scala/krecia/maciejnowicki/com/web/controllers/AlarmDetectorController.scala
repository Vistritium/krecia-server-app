package krecia.maciejnowicki.com.web.controllers

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.http.scaladsl.server.Route
import krecia.maciejnowicki.com.web.{Controller, ControllerDeps, DiscoverableController}
import org.apache.pekko.http.scaladsl.server.Directives.*
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.{AlarmDetectorCommand, AlarmDetectorService}
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout
import krecia.maciejnowicki.com.detector.AlarmDetectorCommand.GetStateReply
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

import java.time.Instant
import java.util.Locale
import scala.concurrent.duration.*

@DiscoverableController
@Singleton
class AlarmDetectorController @Inject()(
  controllerDeps: ControllerDeps,
  alarmDetectorService: AlarmDetectorService,
  actorSystem: ActorSystem[Nothing]
) extends Controller(controllerDeps) {

  private implicit val timeout: Timeout = 3.seconds
  private implicit val implicitAs: ActorSystem[_] = actorSystem

  override def route: Route = pathPrefix("alarm_detector") {
    get {
      onSuccess(alarmDetectorService.alarmDetectorRef.askWithStatus[GetStateReply](ref => AlarmDetectorCommand.GetState(ref))) { success =>
        completeJson(success.alarmData)
      }
    } ~ (path(Segment) & post) { toState =>
      val normalizedState = toState.toUpperCase(Locale.ROOT)

      normalizedState match {
        case "ON" | "OFF" =>
          val from = if (normalizedState == "OFF") {
            "ON"
          } else "OFF"

          alarmDetectorService.alarmDetectorRef.tell(AlarmDetectorCommand.BinaryStateEventCommand(BinaryStateEvent(
            "local",
            "local",
            from,
            normalizedState,
            None,
            Instant.now()
          )))

          complete("ok")
        case _ =>
          complete(400, "Invalid toState. Supported values: ON, OFF")
      }
    }
  }
}
