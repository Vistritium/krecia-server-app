package krecia.maciejnowicki.com.detector

import krecia.maciejnowicki.com.detector.AlarmState.{OFF, ON}
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import scala.concurrent.duration.*

class AlarmDetectorFormulaForDeviceTest extends AnyFlatSpec with Matchers {

  private val alarmDuration: FiniteDuration = 30.seconds

  private val formula = new AlarmDetectorFormula(
    alarmDuration,
    Seq(
      AlarmConfiguration(2, 3, 3.minutes)
    )
  )

  def instantFromOffset(offset: Duration)(implicit instant: Instant): Instant = instant.minusMillis(offset.toMillis)

  "single on event" should "generate one alarm" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(ON, instantFromOffset(0.seconds)),
        AlarmEvent(OFF, instantFromOffset(10.seconds)),
      )
    )

    formula.getForDevice(testData, now) shouldEqual (formula.DeviceWithAlarms(testData, List(formula.RelevantAlarm(now))))
  }

  "no events" should "generate no alarms" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List()
    )

    formula.getForDevice(testData, now) shouldEqual (formula.DeviceWithAlarms(testData, List()))
  }

  "only off events" should "generate no alarm" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(OFF, instantFromOffset(10.seconds)),
      )
    )

    formula.getForDevice(testData, now) shouldEqual (formula.DeviceWithAlarms(testData, List()))
  }

  "alarm of 20 seconds" should "generate 1 alarms" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(ON, instantFromOffset(0.seconds)),
        AlarmEvent(OFF, instantFromOffset(20.seconds)),
      )
    )

    formula.getForDevice(testData, now).alarms should contain theSameElementsAs List(
      formula.RelevantAlarm(now)
    )

  }

  "alarm of 40 seconds" should "generate 2 alarms" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(ON, instantFromOffset(0.seconds)),
        AlarmEvent(OFF, instantFromOffset(40.seconds)),
      )
    )

    formula.getForDevice(testData, now).alarms should contain theSameElementsAs List(
      formula.RelevantAlarm(now),
      formula.RelevantAlarm(instantFromOffset(30.seconds))
    )

  }

  "two short alarms distanced more than 30 seconds" should "generate 2 alarms" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(ON, instantFromOffset(0.seconds)),
        AlarmEvent(OFF, instantFromOffset(10.seconds)),
        AlarmEvent(ON, instantFromOffset(60.seconds)),
        AlarmEvent(OFF, instantFromOffset(70.seconds)),
      )
    )

    formula.getForDevice(testData, now).alarms should contain theSameElementsAs List(
      formula.RelevantAlarm(now),
      formula.RelevantAlarm(instantFromOffset(60.seconds))
    )

  }

  "one short and one long alarm" should "generate 3 alarms" in {
    implicit val now = Instant.now()
    val testData = AlarmDevice(
      "1",
      List(
        AlarmEvent(ON, instantFromOffset(0.seconds)),
        AlarmEvent(OFF, instantFromOffset(10.seconds)),
        AlarmEvent(ON, instantFromOffset(60.seconds)),
        AlarmEvent(OFF, instantFromOffset(100.seconds)),
      )
    )

    formula.getForDevice(testData, now).alarms should contain theSameElementsAs List(
      formula.RelevantAlarm(now),
      formula.RelevantAlarm(instantFromOffset(60.seconds)),
      formula.RelevantAlarm(instantFromOffset(90.seconds)),
    )

  }


}
