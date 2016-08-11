package actor

import akka.actor.{ActorLogging, Actor}

case object Swap

class Swapper extends Actor with ActorLogging {
  override def receive: Receive = {
    case Swap =>
      log.info("Hi")
      context.become({
        case Swap =>
          log.info("Hey")
          context.unbecome()
      }, discardOld = false)
  }
}
