package actor

import akka.actor.Props
import akka.testkit.TestActorRef

class SwapperTest extends ActorTest {
  "A SwapActor" should "be able to swap actor" in {
    val swapperRef = TestActorRef(Props(classOf[Swapper]))
    swapperRef ! Swap
    expectMsgType[String] should include("Hi")
    swapperRef ! Swap
    expectMsgType[String] should include("Hey")
    swapperRef ! Swap
    expectMsgType[String] should include("Hi")
    swapperRef ! Swap
    expectMsgType[String] should include("Hey")
  }
}
