package krecia.maciejnowicki.com.web.controllers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Route
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.web.{Controller, ControllerDeps, DiscoverableController}
import akka.http.scaladsl.server.Directives.*
import krecia.maciejnowicki.com.alarm.{AlarmDeviceCommand, AlarmDeviceState, AlarmPublisher, AlarmService}
import krecia.maciejnowicki.com.detector.AlarmState
import akka.actor.typed.scaladsl.AskPattern.*
import akka.pattern.StatusReply

import java.time.Instant

@Singleton
@DiscoverableController
class AlarmDeviceController @Inject()(
  controllerDeps: ControllerDeps,
  alarmPublisher: AlarmPublisher,
  alarmService: AlarmService,
  actorSystem: ActorSystem[Nothing],
) extends Controller(controllerDeps) with LazyLogging {

  implicit val implicitAk: ActorSystem[Nothing] = actorSystem


  override def route: Route = pathPrefix("alarm_device") {
    (post & path("send" / Segment)) { stateArg =>
      val alarmState = AlarmState.valueOf(stateArg)
      alarmPublisher.publisher.tell(AlarmDeviceState(alarmState, Instant.now))
      complete("ok")
    } ~ {
      onSuccess(alarmService.alarmDeviceRef.askWithStatus(ref => AlarmDeviceCommand.GetState(ref))) { result =>
        completeJson(result)
      }
    }
  }
}
