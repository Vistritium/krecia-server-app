package krecia.maciejnowicki.com.configuration

import akka.actor.ActorSystem
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.github.pjfanning.`enum`.EnumModule
import com.google.inject.{AbstractModule, Provides, Singleton}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import krecia.maciejnowicki.com.web.ControllerDeps
import net.codingwell.scalaguice.ScalaModule
import org.reflections.Reflections

import scala.concurrent.ExecutionContext

class MainModule(config: Config) extends AbstractModule with LazyLogging {

  override def configure(): Unit =
    new Reflections("krecia.maciejnowicki.com")
      .getTypesAnnotatedWith(classOf[Configuration])
      .forEach { c =>
        logger.debug(s"Installing $c")
        install(c.newInstance().asInstanceOf[AbstractModule])
      }

  @Provides
  @Singleton
  def provideConfig(): Config = config

  @Provides
  @Singleton
  def mapper(): ObjectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(EnumModule)
    mapper.registerModule(new JavaTimeModule())
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    mapper.enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    mapper
  }

  @Provides
  @Singleton
  def executionContext(actorSystem: ActorSystem): ExecutionContext = actorSystem.dispatcher

  @Provides
  @Singleton
  def controllerDeps(mapper: ObjectMapper): ControllerDeps = ControllerDeps(mapper)

}
