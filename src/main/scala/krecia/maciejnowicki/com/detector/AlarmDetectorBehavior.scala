package krecia.maciejnowicki.com.detector

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.pattern.StatusReply
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.alarm.{AlarmDeviceCommand, AlarmService}
import krecia.maciejnowicki.com.detector.AlarmDetectorCommand.GetStateReply
import krecia.maciejnowicki.com.detector.AlarmState.OFF
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

import scala.concurrent.Future
import scala.concurrent.duration.*

@Singleton
class AlarmDetectorBehavior @Inject()(
  alarmDetectorFormula: AlarmDetectorFormula,
  alarmService: AlarmService,
) extends LazyLogging {

  private case class State(alarmStateManager: AlarmStateManager, alarmData: AlarmStateData)

  def alarmDetector(): Behavior[AlarmDetectorCommand] = {
    alarmDetectorBehaviour(State(new AlarmStateManager(alarmDetectorFormula), AlarmStateData(Map.empty, Seq.empty)))
  }
  
  private def teeest = {
    Behaviors.setup[String] { context => 
      
      Behaviors.receiveMessage[String] { msg =>
        context.pipeToSelf[String](Future.successful("")){ result =>
          "completed"
        }
        Behaviors.same[String]
      }
    }
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
          val alarmStateData = state.alarmStateManager.getState
          applyState(alarmStateData)
          alarmDetectorBehaviorActive(state.copy(
            alarmData = alarmStateData
          ))
        }
        case AlarmDetectorCommand.BinaryStateEventCommand(binaryStateEvent) => {
          logger.info(s"Processing ${binaryStateEvent}")
          val alarmStateData = state.alarmStateManager.process(binaryStateEvent)
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
    if (alarmStateData.alarms.nonEmpty) {
      alarmService.alarmDeviceRef ! AlarmDeviceCommand.SetState(AlarmState.ON, alarmStateData.alarms)
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
