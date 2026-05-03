package krecia.maciejnowicki.com.alarm

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import com.google.inject.{Inject, Singleton}
import com.typesafe.scalalogging.LazyLogging
import org.apache.pekko.actor.typed.scaladsl.adapter.*

@Singleton
class AlarmService @Inject()(
  alarmPublisher: AlarmPublisher,
  actorSystem: ActorSystem,
  alarmDevice: AlarmDevice,
) extends LazyLogging {

  val alarmDeviceRef: ActorRef[AlarmDeviceCommand] = actorSystem.spawn(alarmDevice.alarmDevice(), "alarm-device")
  
}