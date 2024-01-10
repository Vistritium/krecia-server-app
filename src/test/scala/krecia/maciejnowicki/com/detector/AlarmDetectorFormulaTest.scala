package krecia.maciejnowicki.com.detector

import krecia.maciejnowicki.com.detector.AlarmState.{OFF, ON}
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.concurrent.duration.*

class AlarmDetectorFormulaTest extends AnyFlatSpec with Matchers {


  private val alarmDuration: FiniteDuration = 30.seconds

  private val singleDeviceConfiguration: AlarmConfiguration = AlarmConfiguration(1, 3, 3.minutes)
  private val multiDeviceConfiguration: AlarmConfiguration = AlarmConfiguration(3, 3, 3.minutes)

  private val formula = new AlarmDetectorFormula(
    alarmDuration,
    Seq(
      singleDeviceConfiguration,
      multiDeviceConfiguration
    )
  )

  def instantFromOffset(offset: Duration)(implicit instant: Instant): Instant = instant.minusMillis(offset.toMillis)

  "singleDeviceConfiguration and single alarm" should "not raise alarm" in {
    implicit val now = Instant.now()

    val device = AlarmDevice("1", List.empty)

    val alarms = Seq(formula.DeviceWithAlarms(device, List(formula.RelevantAlarm(instantFromOffset(10.seconds)))))

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe false
  }

  "singleDeviceConfiguration and two alarms" should "not raise alarm" in {
    implicit val now = Instant.now()

    val device = AlarmDevice("1", List.empty)

    val alarms = Seq(formula.DeviceWithAlarms(device, List(
      formula.RelevantAlarm(instantFromOffset(10.seconds)),
      formula.RelevantAlarm(instantFromOffset(40.seconds)),
    )))

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe false
  }

  "singleDeviceConfiguration and three alarms" should "raise alarm" in {
    implicit val now = Instant.now()

    val device = AlarmDevice("1", List.empty)

    val alarms = Seq(formula.DeviceWithAlarms(device, List(
      formula.RelevantAlarm(instantFromOffset(10.seconds)),
      formula.RelevantAlarm(instantFromOffset(40.seconds)),
      formula.RelevantAlarm(instantFromOffset(80.seconds)),
    )))

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe true
  }

  "singleDeviceConfiguration and five alarms" should "raise alarm" in {
    implicit val now = Instant.now()

    val device = AlarmDevice("1", List.empty)

    val alarms = Seq(formula.DeviceWithAlarms(device, List(
      formula.RelevantAlarm(instantFromOffset(10.seconds)),
      formula.RelevantAlarm(instantFromOffset(40.seconds)),
      formula.RelevantAlarm(instantFromOffset(80.seconds)),
      formula.RelevantAlarm(instantFromOffset(120.seconds)),
      formula.RelevantAlarm(instantFromOffset(180.seconds)),
    )))

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe true
  }

  "singleDeviceConfiguration and 1 alarm each with 2 devices" should "not raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(
      formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("2", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(15.seconds)),
      )),
    )

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe false
  }

  "singleDeviceConfiguration and 1 alarm each with 3 devices" should "raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(
      formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("2", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(15.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("3", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
      )),
    )

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe true
  }

  "singleDeviceConfiguration and old alarms" should "not raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
      formula.RelevantAlarm(instantFromOffset(10.seconds)),
      formula.RelevantAlarm(instantFromOffset(40.seconds)),
      formula.RelevantAlarm(instantFromOffset(4.minutes)),
    )))

    formula.detectForConfiguration(alarms, singleDeviceConfiguration, now) shouldBe false
  }

  "multiDeviceConfiguration with two devices and multiple alarms" should "not raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(
      formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("2", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      ))
    )

    formula.detectForConfiguration(alarms, multiDeviceConfiguration, now) shouldBe false


  }

  "multiDeviceConfiguration with three devices and multiple alarms" should "raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(
      formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("2", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("3", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
      ))
    )

    formula.detectForConfiguration(alarms, multiDeviceConfiguration, now) shouldBe true
  }

  "multiDeviceConfiguration with three devices and multiple alarms but last device old alarm" should "not raise alarm" in {
    implicit val now = Instant.now()

    val alarms = Seq(
      formula.DeviceWithAlarms(AlarmDevice("1", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("2", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(10.seconds)),
        formula.RelevantAlarm(instantFromOffset(40.seconds)),
        formula.RelevantAlarm(instantFromOffset(80.seconds)),
        formula.RelevantAlarm(instantFromOffset(90.seconds)),
      )),
      formula.DeviceWithAlarms(AlarmDevice("3", List.empty), List(
        formula.RelevantAlarm(instantFromOffset(4.minutes)),
      ))
    )

    formula.detectForConfiguration(alarms, multiDeviceConfiguration, now) shouldBe false
  }


}
