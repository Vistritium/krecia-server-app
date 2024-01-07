package krecia.maciejnowicki.com

import com.google.inject.Guice
import krecia.maciejnowicki.com.configuration.MainModule
import krecia.maciejnowicki.com.web.WebServer
import com.typesafe.config.ConfigFactory

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val injector = Guice.createInjector(new MainModule(config))
    injector.getInstance(classOf[Starter]).start()
    injector.getInstance(classOf[WebServer])
  }

}
