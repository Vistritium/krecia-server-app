package krecia.maciejnowicki.com.detector

import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent

class StrongAlertsAlarmDetector extends LazyLogging {

  private var devices: Map[String, AlarmDevice] = Map.empty

  def process(event: BinaryStateEvent): AlarmStateData = {
    if (event.isStrongAlert) {
      val state = if (event.toState.equalsIgnoreCase("on")) AlarmState.ON else AlarmState.OFF
      val newEvent = AlarmEvent(state, event.changed)

      val events = devices.get(event.entityId) match {
        case Some(device) => newEvent :: device.events
        case None => List(newEvent)
      }

      devices = devices.updated(event.entityId, AlarmDevice(event.entityId, events))
    }

    getState
  }

  def getState: AlarmStateData = {
    val activeDevices = devices.filter { case (_, device) =>
      device.events.headOption.exists(_.state == AlarmState.ON)
    }

    AlarmStateData(
      devices,
      activeDevices.keys.toSeq,
      if (activeDevices.nonEmpty) Seq("strong_alert") else Seq.empty
    )
  }
}
