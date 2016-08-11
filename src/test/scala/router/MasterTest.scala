package router

import actor.ActorTest
import actor.EchoActor.Echo
import akka.actor.Props
import akka.routing.Broadcast
import akka.testkit.TestActorRef

class MasterTest extends ActorTest {
  "A Master Router" should "be able to route messages to routees" in {
    val masterRef = TestActorRef(Props[Master], "master")
    /*
    In general, any message sent to a router will be sent onwards to its routees,
    but there is one exception.
    The special Broadcast Messages will send to all of a router's routees
     */
    masterRef ! "001"
    masterRef ! "002"
    masterRef ! "003"
    masterRef ! "004"
    masterRef ! "005"

    masterRef ! Broadcast("Watch out, this message is for all routees.")

    val echoMsgs = receiveWhile() {
      case msg: String => msg
      case Echo(msg) => msg
    }
    echoMsgs.foreach(println)
  }

}
