package krecia.maciejnowicki.com.detector

import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, ReplyInbox}
import akka.pattern.StatusReply
import krecia.maciejnowicki.com.mqtt.BinaryStateEvent
import org.scalatest.flatspec.AnyFlatSpec

import java.time.Instant
import scala.concurrent.duration.Duration
import scala.concurrent.duration.*

class AlarmDetectorTest extends AnyFlatSpec {



  private case class TestEntry(
    device: String,
    toState: String,
    offset: Duration
  )

  private val testData = Seq(
    TestEntry("d1", "off", 3.seconds),
    TestEntry("d1", "on", 1.seconds),
    TestEntry("d2", "off", 10.seconds),
    TestEntry("d2", "on", 1.seconds),
  )

  private def convertToBinaryData(elems: Seq[TestEntry]): Seq[BinaryStateEvent] = {

    val now = Instant.now()

    elems.map(entry => {
      val fromState = if(entry.toState == "on") "off" else "on"

      BinaryStateEvent(
        "state",
        entry.device,
        fromState,
        entry.toState,
        "None",
        now.minusMillis(entry.offset.toMillis)
      )
    })
  }

  "alarm detector" should "work" in {

    val alarmDetector = BehaviorTestKit(new AlarmDetectorBehavior(new AlarmDetectorFormula()).alarmDetector())
    convertToBinaryData(testData).foreach(elem => alarmDetector.run( AlarmDetectorCommand.BinaryStateEventCommand(elem)))

    val response: ReplyInbox[StatusReply[AlarmDetectorCommand.GetStateReply]] = alarmDetector.runAsk(ref => AlarmDetectorCommand.GetState(ref))
    val value = response.receiveReply().getValue

    println(value)



  }


}
