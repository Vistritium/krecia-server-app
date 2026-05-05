package krecia.maciejnowicki.com.mqtt

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy
import com.fasterxml.jackson.databind.annotation.{JsonDeserialize, JsonNaming}

import java.time.Instant
import java.util.Locale

enum AlertStrength {
  case Strong, Weak
}


@JsonNaming(classOf[SnakeCaseStrategy])
case class BinaryStateEvent(
  platform: String,
  entityId: String,
  fromState: String,
  toState: String,
  `for`: Option[String],

  @JsonDeserialize(`using` = classOf[HAInstantDeserializer])
  changed: Instant,
) {
  def alertStrength: AlertStrength =
    if (platform.toUpperCase(Locale.ROOT) == "SENSOR_ALERT") AlertStrength.Strong
    else AlertStrength.Weak

  def isStrongAlert: Boolean = alertStrength == AlertStrength.Strong
  def isWeakAlert: Boolean = alertStrength == AlertStrength.Weak
}

