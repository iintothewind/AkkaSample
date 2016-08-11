package fault

import actor.ActorTest
import akka.actor.{ActorRef, Props, Terminated}
import akka.testkit.TestActorRef


class SupervisionTest extends ActorTest {

  trait Builder {
    val supervisorRef = TestActorRef(Props[Supervisor], "supervisor")
    supervisorRef ! Props[Child]
    val childRef = expectMsgType[ActorRef]
  }


  "A supervisor" should "echo the input value" in new Builder {
    childRef ! 42
    childRef ! "get"
    expectMsg(42)
  }

  it should "resume status when ArithmeticException is thrown" in new Builder {
    childRef ! 42
    childRef ! new ArithmeticException("An arithmetic exception thrown from supervisor")
    childRef ! "get"
    expectMsg(42)
  }

  it should "be restarted when NullPointerException is thrown" in new Builder {
    childRef ! new NullPointerException("A NullPointer exception thrown from supervisor")
    childRef ! "get"
    expectMsg(0)
  }

  it should "be stopped when other Exceptions are thrown" in new Builder {
    watch(childRef)
    childRef ! new IllegalArgumentException("An IllegalArgumentException thrown from supervisor")
    expectMsgPF() { case Terminated(child) => println("child is terminated") }
  }

  it should "handle the exception escalated from its child" in new Builder {
    watch(childRef)
    childRef ! new Exception("crash") // cause Escalate
    expectMsgPF() {
      case t@Terminated(child) if t.existenceConfirmed => println(s"child actor has been restarted")
    }
  }
}
