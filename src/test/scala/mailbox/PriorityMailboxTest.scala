package mailbox

import actor.ActorTest
import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory

class PriorityMailboxTest(_system: ActorSystem) extends ActorTest(_system) {
  def this() = this(ActorSystem("test", ConfigFactory.parseResources("mailbox/mailboxes.conf")))

  "A Logger" should "process inbound messages with a pre-defined priority" in {
    val loggerRef = TestActorRef(Props[PriorityLogger].withMailbox("akka.priority-mailbox"), "priorityLogger")
  }
}
