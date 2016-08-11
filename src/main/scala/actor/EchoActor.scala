package actor

import akka.actor._
import actor.EchoActor.{FindActor, Echo}
import akka.routing.Broadcast

class EchoActor extends Actor with ActorLogging {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = log.info(s"$self is started")

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = log.info(s"$self is stopped")

  override def receive: Receive = {
    case Echo(msg) => sender().tell(Echo(msg), self)
    // Identify(message), message can be anything, which will be return in ActorIdentity(correlation, Some(ref)) if any actor has been found
    case FindActor(path) => context.actorSelection(path) ! Identify(path.toString)
    // correlation is the message contained in Identify(message)
    case ActorIdentity(correlation, Some(ref)) => log.info(s"actor $ref has been found")
    case ActorIdentity(correlation, None) => log.info(s"actor $correlation has not been found")
  }
}

object EchoActor {
  def ref(): Props = Props(classOf[EchoActor])

  sealed case class Echo(msg: String)

  sealed case class FindActor(path: ActorPath)

}