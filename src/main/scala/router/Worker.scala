package router

import actor.EchoActor
import akka.routing.Broadcast

case class ParentMessage(msg: String)

class Worker extends EchoActor {
  override def receive: Receive = super.receive.orElse {
    case msg: String =>
      log.info(s"$self receives text message from sender: ${sender()}")
      sender() ! msg
    case ParentMessage(msg) =>
      log.info(s"$self receives parent message from sender: ${sender()}")
      // use router as a sender to reply the message
      // because Worker is a routee created by its router, so context.parent should be its router
      // sender().tell(msg, context.parent)
      sender().!(msg)(context.parent)
    case Broadcast(msg) =>
      log.info(s"$self receives broadcast message from sender: ${sender()}")
      sender() ! msg
  }
}
