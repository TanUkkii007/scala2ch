package scala2ch.models
package stream

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Flow, Sink}
import org.joda.time.DateTime
import scalikejdbc.{AutoSession, DBSession}


object UserFlow {
  case class Create(name: String, password: String, email: String, createdAt: DateTime)
}

trait UserFlow {
  import UserFlow._
  def createFlow(implicit session: DBSession) = Flow[Create].map {
    case Create(name, password, email, createdAt) => {
      User.create(name, password, email, createdAt)(session)
    }
  }

  def selectFlow(implicit session: DBSession) = Flow[Long].map(User.find(_)(session))

  def findFlow(implicit session: DBSession) = Flow[(String, String)].map { info =>
    import scalikejdbc._
    import User.u
    val (name, password) = info
    sql"SELECT ${u.result.*} FROM ${User as u} WHERE ${User.column.name} = $name AND ${User.column.password} = $password".map(User(u.resultName)).single().apply()
  }

  def create(param: Create)(implicit materializer: ActorMaterializer, session: DBSession = AutoSession) = Source.single(param).via(createFlow).runWith(Sink.head)

  def select(userId: Long)(implicit materializer: ActorMaterializer, session: DBSession = AutoSession) = Source.single(userId).via(selectFlow).runWith(Sink.head)

  def find(name: String, password: String)(implicit materializer: ActorMaterializer, session: DBSession = AutoSession) = Source.single((name, password)).via(findFlow).runWith(Sink.head)
}
