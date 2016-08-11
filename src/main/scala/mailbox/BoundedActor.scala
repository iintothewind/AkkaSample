package mailbox

import actor.EchoActor
import actor.EchoActor.{FindActor, Echo}
import akka.actor.{Props, ActorSystem, ActorIdentity, Identify}
import akka.dispatch.{BoundedMessageQueueSemantics, RequiresMessageQueue}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * with RequiresMessageQueue[BoundedMessageQueueSemantics] does not work when ref is created with:
 * TestActorRef(Props[BoundedActor], "boundedActor")
 */
class BoundedActor
  extends EchoActor with RequiresMessageQueue[BoundedMessageQueueSemantics] {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {}

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {}

  override def receive: Receive = {
    case "dispatcher" => sender() ! context.dispatcher.toString
    // return the mailbox config, but not the actor is really using unless defined in akka.actor.deployment or use props.withMailbox(), why???
    case "mailbox" => log.info(s"context.props.mailbox: ${context.props.mailbox}")
    case Echo(msg) => sender().tell(Echo(msg), self)
    // Identify(message), message can be anything, which will be return in ActorIdentity(correlation, Some(ref)) if any actor has been found
    case FindActor(path) => context.actorSelection(path) ! Identify(path.toString)
    // correlation is the message contained in Identify(message)
    case ActorIdentity(correlation, Some(ref)) =>
      log.info(s"actor $ref has been found")
    case ActorIdentity(correlation, None) =>
      log.info(s"actor $correlation has not been found")
  }
}

object SampleApp extends App {
  val system = ActorSystem(
    "test", ConfigFactory.parseResources("mailbox/mailboxes.conf"))
  val boundedActorRef = system.actorOf(Props[BoundedActor], "boundedActor")
  boundedActorRef ! "mailbox"
  //Await.result(system.terminate(), 3.seconds)
}
