package mailbox

import java.util.concurrent.atomic.LongAdder

import actor.EchoActor
import akka.actor.{PoisonPill, ActorSystem, ActorRef}
import akka.actor.ActorSystem.Settings
import akka.dispatch
import akka.dispatch.UnboundedMailbox
import akka.dispatch.{MailboxType, Envelope}
import com.typesafe.config.Config

class CountableUnboundedMailbox extends UnboundedMailbox.MessageQueue {
  private val counter = new LongAdder()

  override def dequeue(): Envelope = {
    counter.decrement()
    super.dequeue()
  }

  override def enqueue(receiver: ActorRef, handle: Envelope): Unit = {
    counter.increment()
    super.enqueue(receiver, handle)
  }

  override def cleanUp(owner: ActorRef, deadLetters: dispatch.MessageQueue): Unit = {
    counter.reset()
    super.cleanUp(owner, deadLetters)
  }

  def length: Int = counter.intValue()
}

class CountableUnboundedMailboxType(settings: Settings, config: Config) extends MailboxType {
  override def create(owner: Option[ActorRef], system: Option[ActorSystem]): dispatch.MessageQueue = (owner, system) match {
    case (Some(o), Some(s)) =>
      val mailbox = new CountableUnboundedMailbox
      MailboxExtension(s).register(o, mailbox)
      mailbox
    case _ => throw new Exception("no mailbox owner or system given")
  }
}

class CountableMailboxActor extends EchoActor {
  self ! "mailboxSize"
  self ! "Normal msg 001"
  self ! "Normal msg 003"
  self ! "Normal msg 002"

  override def receive: Receive = super.receive.orElse {
    case "mailboxSize" => log.info(s"mailbox size: ${MailboxExtension.size}")
    case otherwise: String => log.info(otherwise)
  }
}
