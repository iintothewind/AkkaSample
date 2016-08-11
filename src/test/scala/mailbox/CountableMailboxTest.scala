package mailbox

import actor.ActorTest
import akka.actor.{Props, ActorSystem}
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory

class CountableMailboxTest(_system: ActorSystem) extends ActorTest(_system) {
  def this() = this(ActorSystem("test", ConfigFactory.parseResources("mailbox/mailboxes.conf")))

  "A CountableMailbox" should "be able to return the size of the mailbox" in {
    val countableMailboxActor = TestActorRef(Props[CountableMailboxActor].withMailbox("akka.countable-unbounded-mailbox"), "countableMailboxActor")
  }
}
