package scala2ch.models

import scalikejdbc._
import org.joda.time.{DateTime}

case class Thread(
  id: Long,
  ownerId: Long,
  title: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Thread.autoSession): Thread = Thread.save(this)(session)

  def destroy()(implicit session: DBSession = Thread.autoSession): Unit = Thread.destroy(this)(session)

}


object Thread extends SQLSyntaxSupport[Thread] {

  override val schemaName = Some("scala2ch")

  override val tableName = "thread"

  override val columns = Seq("id", "owner_id", "title", "created_at", "updated_at")

  def apply(t: SyntaxProvider[Thread])(rs: WrappedResultSet): Thread = apply(t.resultName)(rs)
  def apply(t: ResultName[Thread])(rs: WrappedResultSet): Thread = new Thread(
    id = rs.get(t.id),
    ownerId = rs.get(t.ownerId),
    title = rs.get(t.title),
    createdAt = rs.get(t.createdAt),
    updatedAt = rs.get(t.updatedAt)
  )

  val t = Thread.syntax("t")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Thread] = {
    sql"""select ${t.result.*} from ${Thread as t} where ${t.id} = ${id}"""
      .map(Thread(t.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Thread] = {
    sql"""select ${t.result.*} from ${Thread as t}""".map(Thread(t.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${Thread.table}""".map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Thread] = {
    sql"""select ${t.result.*} from ${Thread as t} where ${where}"""
      .map(Thread(t.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Thread] = {
    sql"""select ${t.result.*} from ${Thread as t} where ${where}"""
      .map(Thread(t.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${Thread as t} where ${where}"""
      .map(_.long(1)).single.apply().get
  }

  def create(
    ownerId: Long,
    title: String,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): Thread = {
    val generatedKey = sql"""
      insert into ${Thread.table} (
        ${column.ownerId},
        ${column.title},
        ${column.createdAt},
        ${column.updatedAt}
      ) values (
        ${ownerId},
        ${title},
        ${createdAt},
        ${updatedAt}
      )
      """.updateAndReturnGeneratedKey.apply()

    Thread(
      id = generatedKey,
      ownerId = ownerId,
      title = title,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def save(entity: Thread)(implicit session: DBSession = autoSession): Thread = {
    sql"""
      update
        ${Thread.table}
      set
        ${column.id} = ${entity.id},
        ${column.ownerId} = ${entity.ownerId},
        ${column.title} = ${entity.title},
        ${column.createdAt} = ${entity.createdAt},
        ${column.updatedAt} = ${entity.updatedAt}
      where
        ${column.id} = ${entity.id}
      """.update.apply()
    entity
  }

  def destroy(entity: Thread)(implicit session: DBSession = autoSession): Unit = {
    sql"""delete from ${Thread.table} where ${column.id} = ${entity.id}""".update.apply()
  }

}
