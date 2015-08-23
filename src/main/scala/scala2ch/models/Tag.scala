package scala2ch.models

import scalikejdbc._
import org.joda.time.{DateTime}

case class Tag(
  id: Long,
  name: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = Tag.autoSession): Tag = Tag.save(this)(session)

  def destroy()(implicit session: DBSession = Tag.autoSession): Unit = Tag.destroy(this)(session)

}


object Tag extends SQLSyntaxSupport[Tag] {

  override val schemaName = Some("scala2ch")

  override val tableName = "tag"

  override val columns = Seq("id", "name", "created_at", "updated_at")

  def apply(t: SyntaxProvider[Tag])(rs: WrappedResultSet): Tag = apply(t.resultName)(rs)
  def apply(t: ResultName[Tag])(rs: WrappedResultSet): Tag = new Tag(
    id = rs.get(t.id),
    name = rs.get(t.name),
    createdAt = rs.get(t.createdAt),
    updatedAt = rs.get(t.updatedAt)
  )

  val t = Tag.syntax("t")

  override val autoSession = AutoSession

  def find(id: Long)(implicit session: DBSession = autoSession): Option[Tag] = {
    sql"""select ${t.result.*} from ${Tag as t} where ${t.id} = ${id}"""
      .map(Tag(t.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[Tag] = {
    sql"""select ${t.result.*} from ${Tag as t}""".map(Tag(t.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${Tag.table}""".map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[Tag] = {
    sql"""select ${t.result.*} from ${Tag as t} where ${where}"""
      .map(Tag(t.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[Tag] = {
    sql"""select ${t.result.*} from ${Tag as t} where ${where}"""
      .map(Tag(t.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${Tag as t} where ${where}"""
      .map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): Tag = {
    val generatedKey = sql"""
      insert into ${Tag.table} (
        ${column.name},
        ${column.createdAt},
        ${column.updatedAt}
      ) values (
        ${name},
        ${createdAt},
        ${updatedAt}
      )
      """.updateAndReturnGeneratedKey.apply()

    Tag(
      id = generatedKey,
      name = name,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def save(entity: Tag)(implicit session: DBSession = autoSession): Tag = {
    sql"""
      update
        ${Tag.table}
      set
        ${column.id} = ${entity.id},
        ${column.name} = ${entity.name},
        ${column.createdAt} = ${entity.createdAt},
        ${column.updatedAt} = ${entity.updatedAt}
      where
        ${column.id} = ${entity.id}
      """.update.apply()
    entity
  }

  def destroy(entity: Tag)(implicit session: DBSession = autoSession): Unit = {
    sql"""delete from ${Tag.table} where ${column.id} = ${entity.id}""".update.apply()
  }

}
