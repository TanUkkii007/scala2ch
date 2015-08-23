package scala2ch.session

import akka.agent.Agent
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext}


case class Session(userId: Long, startTime: DateTime)

class SessionStore[T](initial: Map[String, T])(implicit ec: ExecutionContext) {

  private[this] val agent = Agent(initial)(ec)

  def save(key: String, value: T) = {
    agent.alter(_.updated(key, value))
  }

  def get(key: String) = {
    agent().get(key)
  }
}

object SessionStore {
  def apply[T](initial: Map[String, T] = Map.empty[String, T])(implicit ec: ExecutionContext) = new SessionStore[T](initial)(ec)
}