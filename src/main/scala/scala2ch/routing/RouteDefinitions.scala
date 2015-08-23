package scala2ch
package routing

import akka.http.scaladsl.server.{RejectionHandler, Route}
import akka.http.scaladsl.server.Directives._
import akka.event.Logging

trait RouteDefinitions {
  val routeDefinitions: Seq[BasicRoute]
  val loggingRoute = logRequestResult("request", Logging.InfoLevel)
  lazy val routes: Route = loggingRoute {routeDefinitions.map(_.route).reduce(_ ~ _)}
  //implicit lazy val rejectionHandler: RejectionHandler = (routeDefinitions :+ Nil).map(_.rejectionHandler).reduce(_ orElse _)
}
