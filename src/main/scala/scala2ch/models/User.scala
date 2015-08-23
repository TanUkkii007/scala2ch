package scala2ch.models

import scalikejdbc._
import org.joda.time.{DateTime}

case class User(
  userId: Long,
  name: String,
  password: String,
  email: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None) {

  def save()(implicit session: DBSession = User.autoSession): User = User.save(this)(session)

  def destroy()(implicit session: DBSession = User.autoSession): Unit = User.destroy(this)(session)

}


object User extends SQLSyntaxSupport[User] {

  override val schemaName = Some("scala2ch")

  override val tableName = "user"

  override val columns = Seq("user_id", "name", "password", "email", "created_at", "updated_at")

  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User = apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User = new User(
    userId = rs.get(u.userId),
    name = rs.get(u.name),
    password = rs.get(u.password),
    email = rs.get(u.email),
    createdAt = rs.get(u.createdAt),
    updatedAt = rs.get(u.updatedAt)
  )

  val u = User.syntax("u")

  override val autoSession = AutoSession

  def find(userId: Long)(implicit session: DBSession = autoSession): Option[User] = {
    sql"""select ${u.result.*} from ${User as u} where ${u.userId} = ${userId}"""
      .map(User(u.resultName)).single.apply()
  }

  def findAll()(implicit session: DBSession = autoSession): List[User] = {
    sql"""select ${u.result.*} from ${User as u}""".map(User(u.resultName)).list.apply()
  }

  def countAll()(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${User.table}""".map(rs => rs.long(1)).single.apply().get
  }

  def findBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Option[User] = {
    sql"""select ${u.result.*} from ${User as u} where ${where}"""
      .map(User(u.resultName)).single.apply()
  }

  def findAllBy(where: SQLSyntax)(implicit session: DBSession = autoSession): List[User] = {
    sql"""select ${u.result.*} from ${User as u} where ${where}"""
      .map(User(u.resultName)).list.apply()
  }

  def countBy(where: SQLSyntax)(implicit session: DBSession = autoSession): Long = {
    sql"""select count(1) from ${User as u} where ${where}"""
      .map(_.long(1)).single.apply().get
  }

  def create(
    name: String,
    password: String,
    email: String,
    createdAt: DateTime,
    updatedAt: Option[DateTime] = None)(implicit session: DBSession = autoSession): User = {
    val generatedKey = sql"""
      insert into ${User.table} (
        ${column.name},
        ${column.password},
        ${column.email},
        ${column.createdAt},
        ${column.updatedAt}
      ) values (
        ${name},
        ${password},
        ${email},
        ${createdAt},
        ${updatedAt}
      )
      """.updateAndReturnGeneratedKey.apply()

    User(
      userId = generatedKey,
      name = name,
      password = password,
      email = email,
      createdAt = createdAt,
      updatedAt = updatedAt)
  }

  def save(entity: User)(implicit session: DBSession = autoSession): User = {
    sql"""
      update
        ${User.table}
      set
        ${column.userId} = ${entity.userId},
        ${column.name} = ${entity.name},
        ${column.password} = ${entity.password},
        ${column.email} = ${entity.email},
        ${column.createdAt} = ${entity.createdAt},
        ${column.updatedAt} = ${entity.updatedAt}
      where
        ${column.userId} = ${entity.userId}
      """.update.apply()
    entity
  }

  def destroy(entity: User)(implicit session: DBSession = autoSession): Unit = {
    sql"""delete from ${User.table} where ${column.userId} = ${entity.userId}""".update.apply()
  }

}
