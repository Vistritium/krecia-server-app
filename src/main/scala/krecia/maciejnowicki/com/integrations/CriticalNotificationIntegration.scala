package krecia.maciejnowicki.com.integrations

import com.google.inject.ImplementedBy
import krecia.maciejnowicki.com.integrations.Alertzy.AlertzyService
import krecia.maciejnowicki.com.integrations.twilio.CallPhoneService

import scala.concurrent.Future

@ImplementedBy(classOf[CallPhoneService])
trait CriticalNotificationIntegration {

  def send(): Future[Unit]

}
