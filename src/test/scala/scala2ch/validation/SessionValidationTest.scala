package scala2ch.validation

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Sink}
import akka.testkit.TestKit
import org.joda.time.DateTime
import org.scalatest.{MustMatchers, WordSpecLike}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala2ch.session.Session

class SessionValidationTest extends TestKit(ActorSystem("SessionValidationTest")) with WordSpecLike with MustMatchers with SessionValidation {
  "SessionValidation" must {
    import SessionValidation.Errors._
    implicit val materializer = ActorMaterializer()
    val now = DateTime.now()

    "validate existence of session" in {
      val someResult = Source.single(Some(Session(1, now))).via(validateSessionId).runWith(Sink.head)
      Await.result(someResult, 5 seconds) must be(Right(Session(1, now)))
      val noneResult = Source.single(None).via(validateSessionId).runWith(Sink.head)
      Await.result(noneResult, 5 seconds) must be(Left(unknownSession))
    }

    "validate timeout" in {
      val okResult = Source.single(Right(Session(1, now.minus((23.hours + 59.minutes + 59.seconds).toMillis)))).via(validateSessionTimeout(24 hours)(now)).runWith(Sink.head)
      Await.result(okResult, 5 seconds) must be(Right(Session(1, now.minus((23.hours + 59.minutes + 59.seconds).toMillis))))

      val failedResult = Source.single(Right(Session(1, now.minus(24.hours.toMillis)))).via(validateSessionTimeout(24 hours)(now)).runWith(Sink.head)
      Await.result(failedResult, 5 seconds) must be(Left(sessionTimeout))

    }
  }
}