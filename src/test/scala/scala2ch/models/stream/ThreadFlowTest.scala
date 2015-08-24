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
import akka.stream.scaladsl.{Source, Flow, Sink}

class ThreadFlowTest  extends TestKit(ActorSystem("ThreadFlowITest"))
with WordSpecLike with MustMatchers with AutoRollback with TestConnectionPool {

  "ThreadFlow" must {

    object ThreadStream extends ThreadFlow

    val now = DateTime.now()

    implicit val materializer = ActorMaterializer()

    "create thread with createFlow and select with selectFlow" in { implicit session =>
      val result = Await.result(Source.single(ThreadFlow.Create(100, "title", now)).via(ThreadStream.createFlow).runWith(Sink.head), 5 seconds)
      (result.ownerId, result.title, result.createdAt) must be(100, "title", now)
      val selectResult = Await.result(ThreadStream.select(result.id), 5 seconds)
      selectResult.get.thread must be(Thread(result.id, 100, "title", now))
    }

    "create thread" in { implicit session =>
      val stream = ThreadStream.createWithTag(ThreadFlow.Create(1, "title", now), List("tag1", "tag2", "tag3").map(TagFlow.Create(_, now)))
      val result = Await.result(stream, 5 seconds)
      result.thread must be(Thread(result.thread.id, 1, "title", now))
      result.tags.map(_.name) must be(Seq("tag1", "tag2", "tag3"))
      result.threadTags must be(Seq(ThreadTag(result.thread.id,result.tags(0).id,now), ThreadTag(result.thread.id,result.tags(1).id,now), ThreadTag(result.thread.id,result.tags(2).id,now)))
    }

    "select thread with tag" in { implicit session =>
      val thread = ThreadStream.createWithTag(ThreadFlow.Create(1, "title", now), List("tag1", "tag2", "tag3").map(TagFlow.Create(_, now)))
      val threadResult = Await.result(thread, 5 seconds)
      val result = Await.result(ThreadStream.select(threadResult.thread.id), 5 seconds)
      result.get.thread must be(Thread(threadResult.thread.id, 1, "title", now))
      result.get.tags.map(_.name) must be(Seq("tag1", "tag2", "tag3"))
    }
  }
}
