package krecia.maciejnowicki.com.web.controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import krecia.maciejnowicki.com.web.{Controller, ControllerDeps, DiscoverableController}
import akka.http.scaladsl.server.Directives.*
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.{AlarmDetectorCommand, AlarmDetectorService}
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import krecia.maciejnowicki.com.detector.AlarmDetectorCommand.GetStateReply

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

  override def route: Route = path("alarm_detector") {
    onSuccess(alarmDetectorService.alarmDetectorRef.askWithStatus[GetStateReply](ref => AlarmDetectorCommand.GetState(ref))) { success =>
      completeJson(success.alarmData)
    }


  }
}
