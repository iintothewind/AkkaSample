package router

import actor.ActorTest
import akka.actor.Props
import akka.testkit.TestActorRef

class RouteeTest extends ActorTest {
  "A routee" should "be able to set sender as its router when reply" in {
    val masterRef = TestActorRef(Props[Master], "master")
    val msg = "routee will set its router as sender to rely this message"
    masterRef ! ParentMessage(msg)
    expectMsgType[String] should be(msg)
    lastSender.path.name should include("master")
  }

}
