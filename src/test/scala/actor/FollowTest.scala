package actor

import actor.Follower.FindActor
import akka.actor.Props
import akka.testkit.TestActorRef

class FollowTest extends ActorTest {
  "A Follower Actor" should "become other actor" in {
    val echoActor = TestActorRef(EchoActor.ref(), "echoActor")
    val follower = TestActorRef(Props(classOf[Follower]), "follower")
    println(s"parent: ${follower.path.parent}")
    println(s"root: ${follower.path.root}")
    follower ! FindActor(echoActor.path)
  }
}
