package test

import actor.{ActorTest, LogPrintActor}
import akka.actor.{Props, UnhandledMessage}
import akka.testkit.{EventFilter, TestActorRef}

class SideEffectActorTest extends ActorTest {
  "A LogPrintActor" should "print messages when it receives them" in {
    val logPrintActor = TestActorRef(Props[LogPrintActor])
    EventFilter.info(pattern = "(.*)(Test)(.*)", occurrences = 1).intercept {
      logPrintActor ! "Test"
    }
  }

  it should "not handle message with non-string types" in {
    val logPrintActor = TestActorRef(Props[LogPrintActor], "logPrintActor")
    system.eventStream.subscribe(self, classOf[UnhandledMessage])
    logPrintActor ! 1234
    expectMsg(UnhandledMessage(1234, self, logPrintActor))
  }
}
