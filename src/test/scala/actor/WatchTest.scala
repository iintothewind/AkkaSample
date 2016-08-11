package actor

import akka.actor.Props
import akka.testkit.TestActorRef

class WatchTest extends ActorTest {
  "An WatchActor" should "be able to monitor the termination of its watching actor" in {
    val watchActorRef = TestActorRef(Props(classOf[WatchActor]))
    watchActorRef ! "kill"
  }
}
