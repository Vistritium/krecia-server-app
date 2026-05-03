package krecia.maciejnowicki.com

import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import krecia.maciejnowicki.com.configuration.MainModule
import krecia.maciejnowicki.com.integrations.Alertzy.AlertzyService

import scala.concurrent.Await
import scala.concurrent.duration.*

object TestAlertzyCall {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val injector = Guice.createInjector(new MainModule(config))
    val service = injector.getInstance(classOf[AlertzyService])
    Await.result(service.send(), 10.seconds)
  }

}
