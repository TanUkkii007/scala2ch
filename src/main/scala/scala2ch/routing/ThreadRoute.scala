package scala2ch.routing

import akka.actor.{ActorSystem, ActorRefFactory}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ValidationRejection, Rejection}
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol
import scala2ch.Database
import scala2ch.models.User
import scala2ch.models.stream.{TagFlow, ThreadFlow}
import org.joda.time.DateTime
import scala.util.{Success, Failure}

object ThreadRoute {
  def apply()(implicit system: ActorRefFactory, materializer: ActorMaterializer, SessionDirective: SessionDirective) = new ThreadRoute()
}

object ThreadRouteProtocol {
  case class CreateThreadRequest(title: String, tags: Seq[String])
  case class CreateThreadResponse(id: Long, title: String, tags: Seq[String])
  case object ThreadDoesNotExistRejection extends Rejection
}

object ThreadRouteJsonProtocol extends DefaultJsonProtocol {
  import ThreadRouteProtocol._
  implicit val CreateThreadRequestFormat = jsonFormat2(CreateThreadRequest)
  implicit val CreateThreadResponseFormat = jsonFormat3(CreateThreadResponse)
}

class ThreadRoute(implicit system: ActorRefFactory, materializer: ActorMaterializer, SessionDirective: SessionDirective) extends BasicRoute with ThreadFlow {
  import scalikejdbc.AutoSession
  import SessionDirective._
  import ThreadRouteProtocol._
  import SprayJsonSupport._
  import ThreadRouteJsonProtocol._
  implicit val executionContext = system.dispatcher

  val resource = "thread"

  val route = pathPrefix(version / resource) {
    headerValueByName("X-AUTH-TOKEN") { token =>
      authenticate(token)(materializer, AutoSession) { user: User =>
        post {
          entity(as[CreateThreadRequest]) { req =>
            val now = DateTime.now
            val result = Database.default.futureLocalTx { implicit session =>
              createWithTag(ThreadFlow.Create(user.userId, req.title, now), req.tags.map(TagFlow.Create(_, now)).toList)
            }
            onComplete(result) {
              case Success(res) => complete(CreateThreadResponse(res.thread.id, res.thread.title, res.tags.map(_.name)))
              case Failure(e) => {
                complete(e.getMessage)
              }
            }
          }
        } ~
        path(LongNumber) { threadId =>
          delete {
            onComplete(deleteThread(threadId, user.userId)(AutoSession, materializer)) {
              case Success(num) => complete(num.toString)
              case Failure(e) => reject
            }
          }
        }
      } ~
      path(LongNumber){ threadId =>
        get {
          onComplete(select(threadId)(AutoSession, materializer)) {
            case Success(Some(res)) => complete(CreateThreadResponse(res.thread.id, res.thread.title, res.tags.map(_.name)))
            case Success(None) => reject(ValidationRejection("thread does not exist"))
            case Failure(e) => reject
          }
        }
      }
    }
  }
}
