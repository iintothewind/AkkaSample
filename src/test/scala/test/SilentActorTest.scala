package test

import actor.SilentActor.{GetState, SilentMessage}
import actor.{ActorTest, SilentActor}
import akka.actor.Props
import akka.testkit.TestActorRef

class SilentActorTest extends ActorTest {
  "A SilentActor" should "change state when it receives a message, multi-threaded" in {
    val silentActor = TestActorRef(Props[SilentActor], "silentActor")
    silentActor ! SilentMessage("whisper1")
    silentActor ! SilentMessage("whisper2")
    silentActor ! GetState()
    expectMsg(Vector("whisper1", "whisper2"))
  }
}
