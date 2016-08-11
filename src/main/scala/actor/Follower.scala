package actor

import akka.actor._
import actor.Follower.FindActor


class Follower extends Actor with ActorLogging {
  val identifyId = 1


  override def receive: Receive = {
    case FindActor(path) =>
      context.actorSelection(path) ! Identify(path.toString)
    case ActorIdentity(correlation, Some(ref)) =>
      log.info(s"actor $correlation has been found")
      context.watch(ref)
      context.become(active(ref))
    case ActorIdentity(correlation, None) =>
      log.info(s"actor $correlation has not been found, exit")
      context.stop(self)
  }

  def active(another: ActorRef): Receive = {
    case Terminated(actor) => context.stop(self)
  }
}

object Follower {

  sealed case class FindActor(path: ActorPath)

}