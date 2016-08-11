package mailbox

import akka.actor.ActorSystem.Settings
import akka.actor._
import akka.dispatch.{PriorityGenerator, UnboundedStablePriorityMailbox}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration._

sealed case class HighPriority(msg: String)

sealed case class LowPriority(msg: String)

class PriorityMailbox(settings: Settings, config: Config)
  extends UnboundedStablePriorityMailbox(PriorityGenerator {
    case HighPriority(_) => 0
    case LowPriority(_) => 2
    case PoisonPill => 3
    case otherwise => 1
  }, config.getInt("mailbox-initial-capacity"))

class PriorityLogger extends Actor with ActorLogging {
  self ! LowPriority("low msg 001")
  self ! LowPriority("low msg 003")
  self ! LowPriority("low msg 002")
  self ! HighPriority("High msg 003")
  self ! HighPriority("High msg 002")
  self ! HighPriority("High msg 001")
  self ! "Normal msg 001"
  self ! "Normal msg 003"
  self ! "Normal msg 002"
  self ! "mailbox"
  self ! PoisonPill

  override def receive: Receive = {
    case HighPriority(msg) => log.info(s"$msg")
    case LowPriority(msg) => log.info(s"$msg")
    case "mailbox" => log.info(s"context.props.mailbox: ${context.props.mailbox}")
    case msg => log.info(s"$msg")
  }
}

object PriorityMailboxApp extends App {
  val system = ActorSystem("test", ConfigFactory.parseResources("mailbox/mailboxes.conf"))
  val priorityLoggerRef = system.actorOf(Props[PriorityLogger], "priorityLogger")

  //Await.result(system.terminate(), 10.seconds)
}