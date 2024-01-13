package krecia.maciejnowicki.com.alarm

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.StatusReply
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.AlarmState
import krecia.maciejnowicki.com.detector.AlarmState.OFF

import java.time.Instant

case class AlarmDeviceState(
  currentState: AlarmState,
  stateChange: Instant,
)

@Singleton
class AlarmDevice @Inject()(
  alarmPublisher: AlarmPublisher
) extends LazyLogging {

  def alarmDevice(): Behavior[AlarmDeviceCommand] = {
    val statusChangesListeners: Seq[ActorRef[AlarmDeviceState]] = Seq(alarmPublisher.publisher)

    def alarmDeviceBehavior(state: AlarmDeviceState): Behavior[AlarmDeviceCommand] = {
      Behaviors.setup { context =>
        Behaviors.receiveMessage {
          case AlarmDeviceCommand.GetState(replyTo) => {
            replyTo ! StatusReply.Success(state)
            Behaviors.same
          }
          case AlarmDeviceCommand.SetState(alarmState) => {
            if (state.currentState != alarmState) {
              val newState = state.copy(
                currentState = alarmState,
                stateChange = Instant.now()
              )
              statusChangesListeners.foreach(_ ! newState)
              alarmDeviceBehavior(newState)
            } else {
              Behaviors.same
            }
          }
        }
      }
    }

    val state = AlarmDeviceState(OFF, Instant.now())
    statusChangesListeners.foreach(_ ! state)
    alarmDeviceBehavior(state)

  }


}


trait AlarmDeviceCommand

object AlarmDeviceCommand {

  case class SetState(alarmState: AlarmState) extends AlarmDeviceCommand
  case class GetState(replyTo: ActorRef[StatusReply[AlarmDeviceState]]) extends AlarmDeviceCommand

}