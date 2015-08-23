package scala2ch

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import scalikejdbc.ConnectionPool
import scala2ch.routing._

object Application extends App with RouteDefinitions {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  Class.forName("org.h2.Driver")
  ConnectionPool.singleton("jdbc:h2:file:./target/h2db;MODE=MySQL", "root", "passwd")

  implicit object SessionDirective extends SessionDirective

  val routeDefinitions = Seq(IndexRoute(), UserRoute(), LoginRoute(), ThreadRoute())

  Http().bindAndHandle(routes, "localhost", 8080).onSuccess {
    case _ => println("listening on port 8080")
  }
}
