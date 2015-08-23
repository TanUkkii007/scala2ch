package scala2ch
package validation

import session.Session
import akka.stream.scaladsl.{Flow}
import org.joda.time.DateTime
import scala.concurrent.duration.Duration

trait SessionValidation {
  import SessionValidation.Errors._
  val validateSessionId = Flow[Option[Session]].map {
    case Some(session) => Right(session)
    case None => Left(unknownSession)
  }

  def validateSessionTimeout(timeout: Duration)(now: DateTime) = Flow[Either[FailedValidation, Session]].map {
    case Right(session) => {
      val timeoutDay = session.startTime.plus(timeout.toMillis)
      if (now.getMillis < timeoutDay.getMillis) {
        Right(session)
      } else {
        Left(sessionTimeout)
      }
    }
    case failed@Left(_) => failed
  }
}

object SessionValidation {
  object Errors {
    val unknownSession = FailedValidation(1, "unknown session")
    val sessionTimeout = FailedValidation(2, "session timeout")
  }
}
