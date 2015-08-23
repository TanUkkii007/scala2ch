package scala2ch
package routing

import akka.actor.{ActorRefFactory, ActorContext}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._

class IndexRoute(system: ActorRefFactory) extends BasicRoute {
  val resource = ""

  val route = (pathSingleSlash | path(version)) {
    get {
      complete {
        <html>
          <body>
            <h1>Scala 2ch</h1>
          </body>
        </html>
      }
    }
  }
}

object IndexRoute {
  def apply()(implicit system: ActorRefFactory) = new IndexRoute(system)
}