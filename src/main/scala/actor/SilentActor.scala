package actor

import actor.SilentActor.{GetState, SilentMessage}
import akka.actor.Actor

object SilentActor {

  sealed case class SilentMessage(data: String)

  sealed case class GetState()

}

class SilentActor extends Actor {
  var internalState = Vector[String]()

  override def receive: Receive = {
    case SilentMessage(data) => internalState :+= data
    case GetState() => sender() ! internalState // Vector[String] is immutable, its completely safe.
  }
}
