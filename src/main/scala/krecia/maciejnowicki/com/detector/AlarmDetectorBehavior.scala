package krecia.maciejnowicki.com.detector

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.StatusReply
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.alarm.{AlarmDeviceCommand, AlarmService}
import krecia.maciejnowicki.com.detector.AlarmDetectorCommand.GetStateReply
import krecia.maciejnowicki.com.detector.AlarmState.OFF
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

@Singleton
class AlarmDetectorBehavior @Inject()(
  alarmDetectorFormula: AlarmDetectorFormula,
  alarmService: AlarmService,
) extends LazyLogging {

  private case class State(alarmStateManager: AlarmStateManager, alarmData: AlarmStateData)

  def alarmDetector(): Behavior[AlarmDetectorCommand] = {
    alarmDetectorBehavior(State(new AlarmStateManager(alarmDetectorFormula), AlarmStateData(Map.empty, Seq.empty)))
  }

  private def alarmDetectorBehavior(state: State): Behavior[AlarmDetectorCommand] = {
    Behaviors.receiveMessage {
      case AlarmDetectorCommand.BinaryStateEventCommand(binaryStateEvent) => {
        logger.info(s"Processing ${binaryStateEvent}")
        val alarmStateData = state.alarmStateManager.process(binaryStateEvent)
        if (alarmStateData.alarms.nonEmpty) {
          alarmService.alarmDeviceRef ! AlarmDeviceCommand.SetState(AlarmState.ON)
        } else {
          alarmService.alarmDeviceRef ! AlarmDeviceCommand.SetState(AlarmState.OFF)
        }

        alarmDetectorBehavior(state.copy(
          alarmData = alarmStateData
        ))
      }
      case AlarmDetectorCommand.GetState(replyTo) => {
        replyTo ! StatusReply.Success(GetStateReply(state.alarmData))
        Behaviors.same
      }
    }
  }

}

sealed trait AlarmDetectorCommand

object AlarmDetectorCommand {

  case class BinaryStateEventCommand(binaryStateEvent: BinaryStateEvent) extends AlarmDetectorCommand
  case class GetState(replyTo: ActorRef[StatusReply[GetStateReply]]) extends AlarmDetectorCommand
  case class GetStateReply(alarmData: AlarmStateData)

}
