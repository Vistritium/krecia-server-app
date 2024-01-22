package krecia.maciejnowicki.com

import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import krecia.maciejnowicki.com.configuration.MainModule
import krecia.maciejnowicki.com.integrations.twilio.CallPhoneService
import krecia.maciejnowicki.com.integrations.vonage.VonageCallPhoneClient
import krecia.maciejnowicki.com.web.WebServer

object TestVonageCall {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val injector = Guice.createInjector(new MainModule(config))
    val service = injector.getInstance(classOf[VonageCallPhoneClient])
    service.call()
  }

}
