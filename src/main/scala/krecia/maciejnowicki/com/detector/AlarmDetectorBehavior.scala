package krecia.maciejnowicki.com.detector

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.pattern.StatusReply
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.alarm.{AlarmDeviceCommand, AlarmService}
import krecia.maciejnowicki.com.detector.AlarmDetectorCommand.GetStateReply
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

import scala.concurrent.duration.*

@Singleton
class AlarmDetectorBehavior @Inject()(
  weakAlertsAlarmDetectorFormula: WeakAlertsAlarmDetectorFormula,
  alarmService: AlarmService,
) extends LazyLogging {

  private case class State(
    weakAlertsAlarmDetector: WeakAlertsAlarmDetector,
    strongAlertsAlarmDetector: StrongAlertsAlarmDetector,
    alarmData: AlarmStateData
  )

  def alarmDetector(): Behavior[AlarmDetectorCommand] = {
    alarmDetectorBehaviour(State(
      new WeakAlertsAlarmDetector(weakAlertsAlarmDetectorFormula),
      new StrongAlertsAlarmDetector,
      AlarmStateData.Empty
    ))
  }

  private def alarmDetectorBehaviour(state: State): Behavior[AlarmDetectorCommand] = {
    Behaviors.withTimers { scheduler =>
      scheduler.startTimerWithFixedDelay(AlarmDetectorCommand.Refresh, 1.seconds, 1.minutes)

      alarmDetectorBehaviorActive(state)
    }
  }

  private def alarmDetectorBehaviorActive(state: State): Behavior[AlarmDetectorCommand] = {
    //    Behaviors.withTimers { scheduler =>
    //      scheduler.startTimerWithFixedDelay(AlarmDetectorCommand.Refresh, 1.seconds, 1.minutes)

    Behaviors.setup { ctx =>

      Behaviors.receiveMessage {
        case AlarmDetectorCommand.Refresh => {
          val alarmStateData = AlarmStateData.combine(
            state.weakAlertsAlarmDetector.getState,
            state.strongAlertsAlarmDetector.getState
          )
          applyState(alarmStateData)
          alarmDetectorBehaviorActive(state.copy(
            alarmData = alarmStateData
          ))
        }
        case AlarmDetectorCommand.BinaryStateEventCommand(binaryStateEvent) => {
          logger.info(s"Processing ${binaryStateEvent}")
          val weakAlertsState = state.weakAlertsAlarmDetector.process(binaryStateEvent)
          val strongAlertsState = state.strongAlertsAlarmDetector.process(binaryStateEvent)
          val alarmStateData = AlarmStateData.combine(weakAlertsState, strongAlertsState)
          applyState(alarmStateData)

          alarmDetectorBehaviorActive(state.copy(
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

  private def applyState(alarmStateData: AlarmStateData): Unit = {
    if (alarmStateData.triggeredByAlarms.nonEmpty) {
      alarmService.alarmDeviceRef ! AlarmDeviceCommand.SetState(AlarmState.ON, alarmStateData.triggeredByAlarms)
    } else {
      alarmService.alarmDeviceRef ! AlarmDeviceCommand.SetState(AlarmState.OFF, Seq.empty)
    }
  }
}

sealed trait AlarmDetectorCommand

object AlarmDetectorCommand {

  case class BinaryStateEventCommand(binaryStateEvent: BinaryStateEvent) extends AlarmDetectorCommand
  case class GetState(replyTo: ActorRef[StatusReply[GetStateReply]]) extends AlarmDetectorCommand
  case class GetStateReply(alarmData: AlarmStateData)

  case object Refresh extends AlarmDetectorCommand

}
