package scala2ch

import org.scalatest.BeforeAndAfterAll
import scalikejdbc.{ConnectionPoolSettings, ConnectionPool}
import org.scalatest.Suite

trait TestConnectionPool extends BeforeAndAfterAll { this: Suite =>
  def initializeConnectionPool() = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:file:./target/h2db;MODE=MySQL", "root", "passwd", ConnectionPoolSettings(5, 10, 1000, "select 1 as one"))
  }

  def closeAllConnections() = ConnectionPool.closeAll()

  override def beforeAll() = {
    initializeConnectionPool()
  }

  override def afterAll() = {
    closeAllConnections()
  }
}
