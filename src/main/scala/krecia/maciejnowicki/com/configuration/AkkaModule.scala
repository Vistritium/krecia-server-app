package krecia.maciejnowicki.com.configuration

import org.apache.pekko.actor.{ActorSystem, typed}
import org.apache.pekko.http.scaladsl.{Http, HttpExt}
import org.apache.pekko.stream.{ActorMaterializer, Materializer}
import com.google.inject.{AbstractModule, Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import org.apache.pekko.actor.typed.scaladsl.adapter.*

@Configuration
class AkkaModule extends AbstractModule {

  @Provides
  @Singleton
  def system(): ActorSystem = ActorSystem()

  @Provides
  @Singleton
  def typed(actorSystem: ActorSystem): org.apache.pekko.actor.typed.ActorSystem[Nothing] = actorSystem.toTyped

  @Provides
  @Singleton
  def httpClient(actorSystem: ActorSystem): HttpExt = Http()(actorSystem)

}
