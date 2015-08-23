package scala2ch.models.stream

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import org.joda.time.DateTime
import scalikejdbc.{AutoSession, DBSession}
import scala2ch.models.{Tag, Thread, ThreadTag}
import scalikejdbc._

object ThreadTagFlow {
  case class Result(thread: Thread, tags: Seq[Tag], threadTags: Seq[ThreadTag])
}

trait ThreadTagFlow {
  import ThreadTagFlow._
  def createFlow(implicit session: DBSession) = Flow[(Seq[Tag], Thread)].map {
    case (tags, thread) => {
      val threadTags = tags.map(tag => ThreadTag.create(thread.id, tag.id, tag.createdAt))
      Result(thread, tags, threadTags)
    }
  }
}
