package scala2ch.models.stream

import akka.stream.scaladsl.{Flow}
import org.joda.time.DateTime
import scalikejdbc.{DBSession}
import scala2ch.models.Tag
import scalikejdbc._

object TagFlow {
  case class Create(name: String, createdAt: DateTime)
}

trait TagFlow {
  import TagFlow._
  def createFlow(implicit session: DBSession) = Flow[Seq[Create]].map { tags =>
    tags.map {
      case Create(name, createdAt) =>
        import Tag._
        sql"SELECT ${t.result.*} FROM ${Tag as t} WHERE ${Tag.column.name} = ${name} ".map(Tag(t.resultName)).single().apply() match {
          case Some(tag) => tag
          case None => {
            val id = sql"""INSERT INTO ${Tag.table} (
              ${Tag.column.name},
              ${Tag.column.createdAt}
            ) VALUES (
              ${name},
              ${createdAt}
            )
            """.updateAndReturnGeneratedKey().apply()

            Tag(id, name, createdAt, Some(createdAt))
          }
        }
    }
  }
}
