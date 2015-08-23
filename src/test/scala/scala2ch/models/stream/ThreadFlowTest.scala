package scala2ch.models.stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.joda.time.DateTime
import org.scalatest.MustMatchers
import org.scalatest.fixture.WordSpecLike
import scalikejdbc.scalatest.AutoRollback
import scala.concurrent.Await
import scala2ch.TestConnectionPool
import scala.concurrent.duration._
import scala2ch.models.{Thread, ThreadTag, Tag}

class ThreadFlowTest  extends TestKit(ActorSystem("ThreadFlowITest"))
with WordSpecLike with MustMatchers with AutoRollback with TestConnectionPool {

  "ThreadFlow" must {

    object ThreadStream extends ThreadFlow

    val now = DateTime.now()

    implicit val materializer = ActorMaterializer()

    "create thread" in { implicit session =>
      val stream = ThreadStream.createWithTag(ThreadFlow.Create(1, "title", now), List("tag1", "tag2", "tag3").map(TagFlow.Create(_, now)))
      val result = Await.result(stream, 5 seconds)
      result.thread must be(Thread(1, 1, "title", now))
      result.tags.map(_.name) must be(Seq("tag1", "tag2", "tag3"))
      result.threadTags must be(Seq(ThreadTag(1,1,now), ThreadTag(1,2,now), ThreadTag(1,3,now)))
    }
  }
}
