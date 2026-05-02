package krecia.maciejnowicki.com.mqtt

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

import java.time.format.DateTimeFormatter
import java.time.{Instant, OffsetDateTime}

class HAInstantDeserializer extends StdDeserializer[Instant](classOf[Instant]) {

  private val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSXX")

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Instant = {
    Instant.from(pattern.parse(p.getValueAsString))
  }
}

