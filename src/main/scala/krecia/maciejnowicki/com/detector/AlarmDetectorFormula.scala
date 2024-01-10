package krecia.maciejnowicki.com.detector

import com.google.inject.{Inject, Singleton}
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


class AlarmDetectorFormula (
  alarmDuration: FiniteDuration,
  configurations: Seq[AlarmConfiguration],
) extends LazyLogging {

  private[detector] case class RelevantAlarm(date: Instant)

  private[detector] case class DeviceWithAlarms(device: AlarmDevice, alarms: List[RelevantAlarm])

  private val longestAlarmDuration = configurations.map(_.period).maxBy(_.toMillis)

  def getAlarms(alarmDevices: Seq[AlarmDevice], forTime: Instant): Seq[AlarmConfiguration] = {

    val devices = {
      alarmDevices.map { device =>
        getForDevice(device, forTime)
      }
    }

    logger.info(s"Alarms: " + devices.map { device => device.alarms })
    configurations.filter(config => detectForConfiguration(devices, config, forTime))
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
    val cutoff = forTime.minusMillis(configuration.period.toMillis)
    val alarms = alarmsRaw
      .map { device =>
        device.copy(
          alarms = device.alarms.filterNot(_.date.isBefore(cutoff))
        )
      }
      .filter(_.alarms.nonEmpty)


    if (alarms.isEmpty) false
    else {
      val devicesAlarm = alarms.size >= configuration.devices
      val countAlarm = alarms.flatMap(_.alarms).size >= configuration.alarms

      devicesAlarm && countAlarm
    }

  }


}
