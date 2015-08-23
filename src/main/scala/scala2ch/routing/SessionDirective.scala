package scala2ch.routing

import akka.http.scaladsl.server.directives.{FutureDirectives, RouteDirectives, BasicDirectives}
import akka.http.scaladsl.server.{ValidationRejection, Directive1}
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import scalikejdbc.DBSession
import scala.concurrent.ExecutionContext
import scala2ch.models.User
import scala2ch.models.stream.UserFlow
import scala2ch.session.{Session, SessionStore}
import scala2ch.validation.SessionValidation
import akka.stream.scaladsl.{Source, Sink}
import scala.util.{Success, Failure}
import scala.concurrent.duration._

class SessionDirective(implicit executionContext: ExecutionContext) extends SessionValidation with UserFlow {
  import BasicDirectives._
  import RouteDirectives._
  import FutureDirectives._

  private[this] lazy val sessionStore = SessionStore.apply[Session]()

  def authenticate(token: => String)(implicit materializer: ActorMaterializer, session: DBSession): Directive1[User] = {
    val validation = Source.single(sessionStore.get(token))
      .via(validateSessionId)
      .via(validateSessionTimeout(24 hours)(DateTime.now)).runWith(Sink.head)

    val selectUser = validation.flatMap {
      case Right(Session(id, _)) => select(id)
      case Left(e) => throw new Exception(e.message)
    }

    onComplete(selectUser) flatMap {
      case Success(Some(user)) => provide(user)
      case Success(None) => reject(ValidationRejection("user does not exist"))
      case Failure(e) => reject(ValidationRejection(e.getMessage))
    }
  }

  def login(userName: String, password: String)(implicit materializer: ActorMaterializer, session: DBSession): Directive1[String] = {
    val trySession = find(userName, password).flatMap {
      case Some(user) => {
        val token = "accessToken"
        sessionStore.save(token, Session(user.userId, DateTime.now)).map(_ => token)
      }
      case None => throw new Exception(s"userName or password is incorrect")
    }
    onComplete(trySession).flatMap {
      case Success(sessionId) => provide(sessionId)
      case Failure(e) => reject(ValidationRejection(e.getMessage))
    }
  }
}