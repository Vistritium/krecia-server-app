package krecia.maciejnowicki.com.configuration

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.{AbstractModule, Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule

@Configuration
class AkkaModule extends AbstractModule {

  @Provides
  @Singleton
  def system(): ActorSystem = ActorSystem()

  @Provides
  @Singleton
  def httpClient(actorSystem: ActorSystem): HttpExt = Http()(actorSystem)

}
