package actor

import actor.EchoActor.{FindActor, Echo}
import akka.actor.{Props, PoisonPill}
import akka.testkit.TestActorRef

class EchoTest extends ActorTest {
  "An EchoActor" should "be able to echo a message" in {
    val echoActor = TestActorRef(EchoActor.ref())
    val msg = "actor"
    echoActor ! Echo(msg)
    expectMsgType[Echo].msg should be(msg)
    echoActor ! Echo(msg)
    expectMsgType[Echo].msg should be(msg)
    echoActor ! PoisonPill
  }

  "An EchoActor" should "return ActorIdentity(id, Option(ref))" in {
    val echoActor = TestActorRef(Props[EchoActor], "echoActor")
    val follower = TestActorRef(Props[Follower], "follower")
    echoActor ! FindActor(echoActor.path.parent / "someNotExistingActor")
    echoActor ! FindActor(follower.path)

  }
}
