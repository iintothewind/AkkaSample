package mailbox


import akka.actor._
import com.github.blemale.scaffeine.Scaffeine
import scala.concurrent.duration._

class MailboxExtension(val system: ExtendedActorSystem) extends Extension {

  private val cache = Scaffeine().recordStats().expireAfterWrite(1.hour).build[ActorRef,CountableUnboundedMailbox]()

  /**
   * If it would cause memory leak if it never uses unregister
   */
  def register(actorRef: ActorRef, mailbox: CountableUnboundedMailbox): Unit = cache.put(actorRef, mailbox)

  /**
   * never know when to use it
   */
  def unregister(actorRef: ActorRef): Unit = cache.invalidate(actorRef)

  def size(implicit context: ActorContext): Int = {
    cache.getIfPresent(context.self) match {
      case Some(mailbox) => mailbox.length
      case None => throw new IllegalArgumentException(s"Mailbox not registered for: ${context.self}")
    }
  }
}

object MailboxExtension extends ExtensionId[MailboxExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): MailboxExtension = new MailboxExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = this

  def size(implicit context: ActorContext): Int = MailboxExtension(context.system).size
}