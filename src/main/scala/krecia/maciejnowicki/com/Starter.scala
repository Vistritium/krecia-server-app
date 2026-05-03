package krecia.maciejnowicki.com

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.mqtt.MessagesService
import akka.actor.typed.scaladsl.adapter.*
import krecia.maciejnowicki.com.detector.{AlarmDetectorBehavior, AlarmDetectorCommand, AlarmDetectorService}

@Singleton
class Starter @Inject()(
  config: Config,
  messagesService: MessagesService,
  actorSystem: ActorSystem,
  alarmDetector: AlarmDetectorBehavior,
  alarmDetectorService: AlarmDetectorService
) extends LazyLogging {

  private implicit val implicitAs: ActorSystem = actorSystem

  def start(): Unit = {

    val run = messagesService.source
      .map(binaryState => AlarmDetectorCommand.BinaryStateEventCommand(binaryState))
      .to(Sink.actorRef(alarmDetectorService.alarmDetectorRef.toClassic, "unused", e => {
        logger.error("error", e)
      }))
      .run()

  }
}
