package krecia.maciejnowicki.com.detector

import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.AlarmState.OFF
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

import java.time.Instant
import scala.concurrent.duration.*

enum AlarmState {
  case ON, OFF
}

case class AlarmEvent(
  state: AlarmState,
  datetime: Instant,
)

case class AlarmDevice(
  id: String,
  events: List[AlarmEvent]
)

class AlarmStateManager(
  alarmDetectorFormula: AlarmDetectorFormula
) extends LazyLogging {

  private var devices: Map[String, AlarmDevice] = Map.empty
  private val keepUpTo = 10.minutes

  def process(event: BinaryStateEvent): AlarmStateData = {
    val state = if (event.toState.equalsIgnoreCase("on")) AlarmState.ON else AlarmState.OFF
    val newEvent = AlarmEvent(state, event.changed)

    devices.get(event.entityId) match
      case None => {
        val events = if (newEvent.state == AlarmState.ON) {
          List(newEvent, AlarmEvent(AlarmState.OFF, newEvent.datetime.minusMillis(100)))
        } else {
          List(newEvent)
        }

        devices = devices + ((event.entityId, AlarmDevice(event.entityId, events)))
      }
      case Some(device) => {

        val latestState = device.events.head
        val existingEvents = if (latestState.state == state) {
          device.events.tail
        } else {
          device.events
        }

        devices = devices.updated(
          event.entityId,
          device.copy(
            events = newEvent :: existingEvents
          )
        )
      }

    updateState()

    getState
  }


  private def updateState(currentTime: Instant = Instant.now()): Unit = {

    this.devices = devices.view.mapValues { device =>

      val cutoff = currentTime.minusMillis(keepUpTo.toMillis)

      val (keep, discard) = device.events.partition(_.datetime.isAfter(cutoff))
      val latestKeep = discard.maxByOption(_.datetime.toEpochMilli)
      val discarded = discard.toSet -- latestKeep.toSet
      if (discarded.nonEmpty) {
        logger.info(s"Discarded ${discarded}")
      }

      device.copy(
        events = latestKeep.toList ::: keep
      )
    }.toMap

  }

  def getState: AlarmStateData = {
    val detectedAlarms = alarmDetectorFormula.getAlarms(devices.values.toSeq, Instant.now())
    AlarmStateData(
      devices,
      detectedAlarms.triggeredByAlarms,
      detectedAlarms.triggeredByConfigurations.map(_.name)
    )
  }


}

case class AlarmStateData(
  devices: Map[String, AlarmDevice],
  triggeredByAlarms: Seq[String],
  triggeredByConfigurations: Seq[String]
)
