package actor

import actor.FilteringActor.Event
import akka.actor.Actor

object FilteringActor {

  sealed case class Event(id: Long)

}

class FilteringActor(bufferSize: Int) extends Actor {
  var lastMessages = Vector[Event]()

  override def receive: Receive = {
    case msg: Event => if (!lastMessages.contains(msg)) {
      lastMessages :+= msg
      sender() ! msg
      if (lastMessages.size > bufferSize) {
        lastMessages = lastMessages.tail
      }
    }
  }
}
