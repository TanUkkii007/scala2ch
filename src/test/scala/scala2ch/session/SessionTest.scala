package scala2ch.session

import org.scalatest.{MustMatchers, WordSpecLike}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SessionTest extends WordSpecLike with MustMatchers {
  "Session" must {

    "get key-value pair" in {
      val session = SessionStore(Map("key" -> "value"))
      session.get("key") must be(Some("value"))
      session.get("unknown") must be(None)
    }

    "save and get key-value pair" in {
      val session = SessionStore(Map.empty[String, String])
      Await.result(session.save("key", "value"), 5 seconds)
      session.get("key") must be(Some("value"))
    }
  }
}
