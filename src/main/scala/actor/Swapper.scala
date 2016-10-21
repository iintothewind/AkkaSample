package actor

import akka.actor.{ActorLogging, Actor}

case object Swap

class Swapper extends Actor with ActorLogging {
  override def receive: Receive = {
    case Swap =>
      sender() ! "Hi"
      context.become({
        case Swap =>
          sender() ! "Hey"
          context.unbecome()
      }, discardOld = false)
  }
}
