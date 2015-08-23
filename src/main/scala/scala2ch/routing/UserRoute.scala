package scala2ch.routing

import akka.actor.ActorRefFactory
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import spray.json.DefaultJsonProtocol
import scala2ch.models.stream.UserFlow
import scala2ch.models.User
import scala.util.{Success, Failure}

object UserProtocol {
  case class CreateUserRequest(userName: String, password: String, email: String)
  case class CreateUserResponse(userId: Long, userName: String, password: String, email: String)
}

object UserJsonProtocol extends DefaultJsonProtocol {
  import UserProtocol._
  implicit val CreateUserRequestFormat = jsonFormat3(CreateUserRequest)
  implicit val CreateUserResponseFormat = jsonFormat4(CreateUserResponse)
}

class UserRoute(implicit system: ActorRefFactory, materializer: ActorMaterializer) extends BasicRoute with UserFlow {
  import UserProtocol._
  import SprayJsonSupport._
  import UserJsonProtocol._
  val resource = "users"

  val route = pathPrefix(version / resource) {
    get {
      parameter("id".as[Long]) { userId =>
        onComplete(select(userId)) {
          case Success(Some(User(id, name, password, email, _, _))) => complete(CreateUserResponse(id, name, password, email))
          case Success(None) => complete(s"could not find user with id $userId")
          case Failure(e) => complete(e.getMessage)
        }
      }
    } ~
    post {
      entity(as[CreateUserRequest]) { request =>
        val CreateUserRequest(userName, password, email) = request
        onComplete(create(UserFlow.Create(userName, password, email, DateTime.now))) {
          case Success(User(userId, name, password, email, _, _)) => complete(CreateUserResponse(userId, name, password, email))
          case Failure(e) => complete(e.getMessage)
        }
      }
    }
  }
}

object UserRoute {
  def apply()(implicit system: ActorRefFactory, materializer: ActorMaterializer) = new UserRoute
}