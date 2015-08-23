package scala2ch.models.stream

import akka.stream.{ActorMaterializer}
import akka.stream.scaladsl._
import org.joda.time.DateTime
import scalikejdbc._
import scala2ch.models.{ThreadTag, Thread, Tag}

object ThreadFlow {
  case class Create(ownerId: Long, title: String, createdAt: DateTime)
  case class Delete(id: Long, ownerId: Long)
  case class Result(thread: Thread, tags: List[Tag])
}

trait ThreadFlow {
  import ThreadFlow._

  object TagStream extends TagFlow
  object ThreadTagStream extends ThreadTagFlow

  def selectFlow(implicit session: DBSession) = Flow[Long].map { id =>
    import ThreadTag._
    import Tag._

    Thread.find(id) match {
      case Some(thread) => {
        val tags = sql"SELECT ${t.id}, ${t.name}, ${t.createdAt} FROM ${Tag as t} LEFT JOIN ${ThreadTag as tt} ON ${tt.threadId} = ${t.id} WHERE ${tt.threadId} = $id"
          .map(rs => Tag(rs.long(1), rs.string(2), rs.jodaDateTime(3))).list().apply()
        Some(Result(thread, tags))
      }
      case None => None
    }
  }
  def createFlow(implicit session: DBSession) = Flow[Create].map {
    case Create(ownerId, title, createdAt) => Thread.create(ownerId, title, createdAt)(session)
  }

  def deleteFlow(implicit session: DBSession) = Flow[Delete].map { req =>
    sql"""DELETE FROM ${Thread.table} WHERE ${Thread.column.id} = ${req.id} AND ${Thread.column.ownerId} = ${req.ownerId}""".update.apply()
  }

  def select(id: Long)(implicit session: DBSession, materializer: ActorMaterializer) = Source.single(id).via(selectFlow).runWith(Sink.head)

  def createWithTag(param: Create, tags: List[TagFlow.Create])(implicit session: DBSession, materializer: ActorMaterializer) = {
    FlowGraph.closed(Sink.head[ThreadTagFlow.Result]) { implicit builder =>
      sink =>
      import FlowGraph.Implicits._
      val tagIn = Source.single(tags)
      val threadIn = Source.single(param)
      val zip = builder.add(Zip[Seq[Tag], Thread]())
      val tagFlow = builder.add(TagStream.createFlow)
      val threadFlow = builder.add(createFlow)
      val threadTagFlow = builder.add(ThreadTagStream.createFlow)
      tagIn ~> tagFlow ~> zip.in0
      threadIn ~> threadFlow ~> zip.in1
      zip.out ~> threadTagFlow.inlet
      threadTagFlow.outlet ~> sink
    }.run()
  }

  def deleteThread(id: Long, userId: Long)(implicit session: DBSession, materializer: ActorMaterializer) = Source.single(Delete(id, userId)).via(deleteFlow).runWith(Sink.head)
}
