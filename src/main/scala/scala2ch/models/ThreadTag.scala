package scala2ch.models

import scalikejdbc._
import org.joda.time.{DateTime}

case class ThreadTag(
  threadId: Long,
  tagId: Long,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = ThreadTag.autoSession): ThreadTag = ThreadTag.save(this)(session)

  def destroy()(implicit session: DBSession = ThreadTag.autoSession): Unit = ThreadTag.destroy(this)(session)

}


object ThreadTag extends SQLSyntaxSupport[ThreadTag] {

  override val schemaName = Some("scala2ch")

  override val tableName = "thread_tag"

  override val columns = Seq("thread_id", "tag_id", "created_at", "updated_at")

  def apply(tt: SyntaxProvider[ThreadTag])(rs: WrappedResultSet): ThreadTag = apply(tt.resultName)(rs)
  def apply(tt: ResultName[ThreadTag])(rs: WrappedResultSet): ThreadTag = new ThreadTag(
    threadId = rs.get(tt.threadId),
    tagId = rs.get(tt.tagId),
    createdAt = rs.get(tt.createdAt),
    updatedAt = rs.get(tt.updatedAt)
  )

  val tt = ThreadTag.syntax("tt")

  override val autoSession = AutoSession

  def find(tagId: Long, threadId: Long)(implicit session: DBSession = autoSession): Option[ThreadTag] = {
    sql"""select ${tt.result.*} from ${ThreadTag as tt} where ${tt.tagId} = ${tagId} and ${tt.threadId} = ${threadId}"""
      .map(ThreadTag(tt.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[ThreadTag] = {
    sql"""select ${tt.result.*} from ${ThreadTag as tt}""".map(ThreadTag(tt.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${ThreadTag.table}""".map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[ThreadTag] = {
    sql"""select ${tt.result.*} from ${ThreadTag as tt} where ${where}"""
      .map(ThreadTag(tt.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[ThreadTag] = {
    sql"""select ${tt.result.*} from ${ThreadTag as tt} where ${where}"""
      .map(ThreadTag(tt.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${ThreadTag as tt} where ${where}"""
      .map(_.long(1)).single.apply().get
  }

  def create(
    threadId: Long,
    tagId: Long,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): ThreadTag = {
    sql"""
      insert into ${ThreadTag.table} (
        ${column.threadId},
        ${column.tagId},
        ${column.createdAt},
        ${column.updatedAt}
      ) values (
        ${threadId},
        ${tagId},
        ${createdAt},
        ${updatedAt}
      )
      """.update.apply()

    ThreadTag(
      threadId = threadId,
      tagId = tagId,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def save(entity: ThreadTag)(implicit session: DBSession = autoSession): ThreadTag = {
    sql"""
      update
        ${ThreadTag.table}
      set
        ${column.threadId} = ${entity.threadId},
        ${column.tagId} = ${entity.tagId},
        ${column.createdAt} = ${entity.createdAt},
        ${column.updatedAt} = ${entity.updatedAt}
      where
        ${column.tagId} = ${entity.tagId} and ${column.threadId} = ${entity.threadId}
      """.update.apply()
    entity
  }

  def destroy(entity: ThreadTag)(implicit session: DBSession = autoSession): Unit = {
    sql"""delete from ${ThreadTag.table} where ${column.tagId} = ${entity.tagId} and ${column.threadId} = ${entity.threadId}""".update.apply()
  }

}
