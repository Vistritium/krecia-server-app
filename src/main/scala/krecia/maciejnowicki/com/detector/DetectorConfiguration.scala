package krecia.maciejnowicki.com.detector

import com.google.inject.{AbstractModule, Provides}
import krecia.maciejnowicki.com.configuration.Configuration

import scala.concurrent.duration._

@Configuration
class DetectorConfiguration extends AbstractModule {

  @Provides
  def formula: WeakAlertsAlarmDetectorFormula =
    WeakAlertsAlarmDetectorFormula(
      20.seconds,
      Seq(
        AlarmConfiguration(devices = 2, alarms = 3, period = 3.minutes),
        AlarmConfiguration(devices = 3, alarms = 4, period = 10.minutes),
      )
    )

}
