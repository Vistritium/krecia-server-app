package krecia.maciejnowicki.com.detector

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter.*
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging

@Singleton
class AlarmDetectorService @Inject()(
  actorSystem: ActorSystem,
  alarmDetector: AlarmDetectorBehavior,
) extends LazyLogging {

  val alarmDetectorRef = actorSystem.spawn(alarmDetector.alarmDetector(), "alarmDetector")

}
