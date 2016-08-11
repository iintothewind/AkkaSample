package actor

import akka.actor.Props
import akka.testkit.TestActorRef

class SwapTest extends ActorTest {
  "A SwapActor" should "be able to swap actor" in {
    val swapActorRef = TestActorRef(Props(classOf[SwapActor]))
    swapActorRef ! "foo"
    expectMsgType[String] should include("I would become angry")
    swapActorRef ! "foo"
    expectMsgType[String] should include("I am angry")
    swapActorRef ! "bar"
    expectMsgType[String] should include("I would become happy")
    swapActorRef ! "bar"
    expectMsgType[String] should include("I am happy")
    swapActorRef ! "foo"
    expectMsgType[String] should include("I would become angry")
  }
}
