package scala2ch
package routing

import akka.http.scaladsl.server.Route

trait BasicRoute {
  val route: Route
  val resource: String
  val versionNumber = 1
  val version = s"v$versionNumber"
}
