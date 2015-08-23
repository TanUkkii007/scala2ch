package scala2ch.models
package stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.{MustMatchers}
import org.scalatest.fixture.WordSpecLike
import scalikejdbc.scalatest.AutoRollback
import org.joda.time.DateTime
import scala.concurrent.Await
import scala.concurrent.duration._
import scala2ch.TestConnectionPool
import scala2ch.Database

class UserTest extends TestKit(ActorSystem("UserITest"))
with WordSpecLike with MustMatchers with AutoRollback with TestConnectionPool {

  "UserFlow" must {
    import UserFlow._

    val now = DateTime.now()

    implicit val materializer = ActorMaterializer()

    object UserCrud extends UserFlow

    "create" in { implicit session =>
      Await.result(UserCrud.create(Create("username1", "passwd1", "email1", now)), 5 seconds) must be(
        User(1, "username1", "passwd1", "email1", now)
      )
    }

    "select" in { implicit session =>
      Await.result(UserCrud.create(Create("username2", "passwd2", "email2", now)), 5 seconds) must be(
        User(2, "username2", "passwd2", "email2", now)
      )

      Await.result(UserCrud.select(2), 5 seconds) must be(Some(User(2, "username2", "passwd2", "email2", now)))
    }

    "find" in { implicit session =>
      Await.result(UserCrud.create(Create("username3", "passwd3", "email3", now)), 5 seconds) must be(
        User(3, "username3", "passwd3", "email3", now)
      )

      Await.result(UserCrud.find("username3", "passwd3"), 5 seconds) must be(Some(User(3, "username3", "passwd3", "email3", now)))
    }
  }

  override def beforeAll() = {
    import scalikejdbc._
    super.beforeAll()
    Database.default.autoCommit { implicit session =>
      val num = sql"DELETE FROM ${User.table}".update().apply()
      println(num)
    }
  }
}
