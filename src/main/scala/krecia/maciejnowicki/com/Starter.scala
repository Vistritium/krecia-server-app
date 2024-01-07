package krecia.maciejnowicki.com

import com.google.inject.{Inject, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.mqtt.MessagesService

@Singleton
class Starter @Inject()(config: Config, messagesService: MessagesService) extends LazyLogging {
  def start(): Unit = {
    messagesService.go()
  }
}
