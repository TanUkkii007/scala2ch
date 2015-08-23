package scala2ch

import scalikejdbc.{ConnectionPool, NamedDB}

object Database {
  val default = NamedDB(ConnectionPool.DEFAULT_NAME)
}
