package scala2ch

import org.scalatest.BeforeAndAfterAll
import scalikejdbc.ConnectionPool
import org.scalatest.Suite

trait TestConnectionPool extends BeforeAndAfterAll { this: Suite =>
  def initializeConnectionPool() = {
    Class.forName("org.h2.Driver")
    ConnectionPool.singleton("jdbc:h2:file:./target/h2db;MODE=MySQL", "root", "passwd")
  }

  def closeAllConnections() = ConnectionPool.closeAll()

  override def beforeAll() = {
    initializeConnectionPool()
  }

  override def afterAll() = {
    closeAllConnections()
  }
}
