package actor

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest._
import org.slf4s.Logging

import scala.concurrent.Await
import scala.concurrent.duration._

abstract class ActorTest(_system: ActorSystem)
  extends TestKit(_system) with ImplicitSender with Matchers with FlatSpecLike
  with BeforeAndAfterEachTestData with BeforeAndAfterAll with Logging {
  def this() = this(ActorSystem("system"))

  override def afterAll(): Unit = {
    Await.result(system.terminate(), 10.seconds)
  }
}