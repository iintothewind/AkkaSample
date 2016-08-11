package actor

import actor.SendingActor.Game
import akka.actor.Actor

object SendingActor {

  sealed case class Ticket(seat: Int)

  sealed case class Game(name: String, tickets: Seq[Ticket])

}

class SendingActor extends Actor {
  override def receive: Receive = {
    case game@Game(_, tickets) => sender() ! game.copy(tickets = tickets.tail) // create a copy of game, and just change its tickets property
  }
}
