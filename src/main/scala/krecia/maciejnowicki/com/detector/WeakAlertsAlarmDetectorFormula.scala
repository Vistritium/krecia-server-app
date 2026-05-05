package krecia.maciejnowicki.com.detector

import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.detector.AlarmState.ON

import java.time.Instant
import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

case class AlarmConfiguration(
  devices: Int,
  alarms: Int,
  period: Duration,
) {
  def name = s"devices_${devices}_alarms_${alarms}_duration_${period.toMinutes}"
}


class WeakAlertsAlarmDetectorFormula (
  alarmDuration: FiniteDuration,
  configurations: Seq[AlarmConfiguration],
) extends LazyLogging {
  case class AlarmDetectionResult(
    triggeredByConfigurations: Seq[AlarmConfiguration],
    triggeredByAlarms: Seq[String]
  )

  private[detector] case class RelevantAlarm(date: Instant)

  private[detector] case class DeviceWithAlarms(device: AlarmDevice, alarms: List[RelevantAlarm])

  private val longestAlarmDuration = configurations.map(_.period).maxBy(_.toMillis)

  def getAlarms(alarmDevices: Seq[AlarmDevice], forTime: Instant): AlarmDetectionResult = {

    val devices = {
      alarmDevices.map { device =>
        getForDevice(device, forTime)
      }
    }

    logger.info(s"Alarms: " + devices.map { device => device.alarms })
    val triggeredByConfigurations = configurations.filter(config => detectForConfiguration(devices, config, forTime))
    val triggeredByAlarms = triggeredByConfigurations
      .flatMap { configuration =>
        getTriggeredDevicesForConfiguration(devices, configuration, forTime)
      }
      .map(_.device.id)
      .distinct

    AlarmDetectionResult(triggeredByConfigurations, triggeredByAlarms)
  }

  private[detector] def getForDevice(device: AlarmDevice, forTime: Instant): DeviceWithAlarms = {
    val minLimitCutoff = forTime.minusMillis((longestAlarmDuration + 1.seconds).toMillis)

    def cutEventsForNewAlarm(events: List[AlarmEvent], alarmTime: Instant): List[AlarmEvent] = {
      val cutoff = alarmTime.minusMillis(alarmDuration.toMillis)
      val (keep, cut) = events.partition(_.datetime.isBefore(cutoff))
      val boundaryEvent = cut.lastOption.getOrElse(AlarmEvent(ON, cutoff))
      boundaryEvent :: keep
    }

    @tailrec
    def iter(remainingStates: List[AlarmEvent], alarmAggr: List[RelevantAlarm]): List[RelevantAlarm] = {
      if (remainingStates.isEmpty) alarmAggr
      else if (alarmAggr.exists(_.date.isBefore(minLimitCutoff))) alarmAggr
      else {
        val (current, remaining) = (remainingStates.head, remainingStates.tail)
        if (current.state == AlarmState.ON) {
          val alarmTime = current.datetime
          val cutEvents = cutEventsForNewAlarm(remaining, alarmTime)
          iter(cutEvents, RelevantAlarm(alarmTime) :: alarmAggr)
        } else {
          iter(remaining, alarmAggr)
        }
      }
    }

    val alarms = iter(device.events.sortBy(_.datetime.toEpochMilli).reverse, List.empty)
    DeviceWithAlarms(device, alarms)
  }

  private[detector] def detectForConfiguration(alarmsRaw: Seq[DeviceWithAlarms], configuration: AlarmConfiguration, forTime: Instant): Boolean = {
    val alarms = getTriggeredDevicesForConfiguration(alarmsRaw, configuration, forTime)

    if (alarms.isEmpty) false
    else {
      val devicesAlarm = alarms.size >= configuration.devices
      val countAlarm = alarms.flatMap(_.alarms).size >= configuration.alarms

      devicesAlarm && countAlarm
    }
  }

  private[detector] def getTriggeredDevicesForConfiguration(alarmsRaw: Seq[DeviceWithAlarms], configuration: AlarmConfiguration, forTime: Instant): Seq[DeviceWithAlarms] = {
    val cutoff = forTime.minusMillis(configuration.period.toMillis)
    alarmsRaw
      .map { device =>
        device.copy(
          alarms = device.alarms.filterNot(_.date.isBefore(cutoff))
        )
      }
      .filter(_.alarms.nonEmpty)
  }


}
