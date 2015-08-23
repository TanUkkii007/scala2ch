package scala2ch.routing

import akka.actor.ActorRefFactory
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import scalikejdbc.AutoSession
import spray.json.DefaultJsonProtocol
import scala.util.{Success, Failure}
import scala2ch.models.stream.UserFlow

object LoginProtocol {
  case class LoginRequest(userName: String, password: String)
}

object LoginJsonProtocol extends DefaultJsonProtocol {
  import LoginProtocol._
  implicit val LoginRequestFormat = jsonFormat2(LoginRequest)
}

class LoginRoute(implicit system: ActorRefFactory, materializer: ActorMaterializer, SessionDirective: SessionDirective) extends BasicRoute with UserFlow {
  import LoginProtocol._
  import SprayJsonSupport._
  import LoginJsonProtocol._
  import SessionDirective._
  import system.dispatcher

  val resource = "login"

  val route = pathPrefix(version / resource) {
    post {
      entity(as[LoginRequest]) { req =>
        login(req.userName, req.password)(materializer, AutoSession) { token =>
          complete(token)
        }
      }
    }
  }
}

object LoginRoute {
  def apply()(implicit system: ActorRefFactory, materializer: ActorMaterializer, SessionDirective: SessionDirective) = new LoginRoute
}