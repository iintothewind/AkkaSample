package mailbox

import actor.ActorTest
import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory

class SettingMailboxesTest(_system: ActorSystem) extends ActorTest(_system) {
  def this() = this(ActorSystem("test", ConfigFactory.parseResources("mailbox/mailboxes.conf")))

  "A BoundedMailbox" should "be used when creating actor with BoundedMessageQueueSemantics" in {
    // with RequiresMessageQueue[BoundedMessageQueueSemantics] does not work when ref is created with:
    // TestActorRef(Props[BoundedActor], "boundedActor")
    val boundedActorRef = TestActorRef(Props[BoundedActor].withMailbox("akka.bounded-mailbox"), "boundedActor")
    boundedActorRef ! "mailbox"
    expectMsgPF() { case s: String => s shouldBe "akka.bounded-mailbox" }
  }

  it should "be used in dispatcher when mailbox-requirement is BoundedMessageQueueSemantics" in {
    val boundedActorRef = TestActorRef(Props[BoundedActor].withDispatcher("akka.pinned-dispatcher"), "boundedActor")
    //boundedActorRef ! "mailbox"
    //expectMsgPF() { case s: String => s shouldBe "akka.bounded-mailbox" }
  }
}
