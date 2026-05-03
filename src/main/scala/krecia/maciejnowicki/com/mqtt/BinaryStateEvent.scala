package krecia.maciejnowicki.com.mqtt

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonNaming}

import java.time.Instant


@JsonNaming(classOf[SnakeCaseStrategy])
case class BinaryStateEvent(
  platform: String,
  entityId: String,
  fromState: String,
  toState: String,
  `for`: Option[String],

  @JsonDeserialize(`using` = classOf[HAInstantDeserializer])
  changed: Instant,
)


