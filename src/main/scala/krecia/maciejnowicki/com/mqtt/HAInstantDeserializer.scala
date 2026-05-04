package krecia.maciejnowicki.com.mqtt

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.{Instant, OffsetDateTime}
import scala.util.Try

class HAInstantDeserializer extends StdDeserializer[Instant](classOf[Instant]) {

  private val basicOffsetIsoDateTime = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .appendOffset("+HHMM", "Z")
    .toFormatter

  private val isoDateTimeFormatters = Seq(
    DateTimeFormatter.ISO_OFFSET_DATE_TIME,
    basicOffsetIsoDateTime,
  )

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Instant = {
    val value = p.getText.trim

    isoDateTimeFormatters.view
      .flatMap(formatter => Try(OffsetDateTime.parse(value, formatter).toInstant).toOption)
      .headOption
      .getOrElse {
        throw ctxt.weirdStringException(
          value,
          classOf[Instant],
          "Expected ISO-8601 offset date-time, for example 2026-05-04T18:32:05.680636+0200",
        )
      }
  }
}
