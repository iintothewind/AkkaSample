package actor

import akka.actor.Actor

class SwapActor extends Actor {

  def angry: Receive = {
    case "foo" => sender ! "I am angry"
    case "bar" => sender ! "I would become happy"; context.become(happy)
  }

  def happy: Receive = {
    case "bar" => sender ! "I am happy"
    case "foo" => sender ! "I would become angry"; context.become(angry)
  }

  override def receive: Receive = {
    case "foo" => sender ! "I would become angry"; context.become(angry)
    case "bar" => sender ! "I would become happy"; context.become(happy)
  }
}


