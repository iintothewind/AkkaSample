package test

import actor.SendingActor.{Game, Ticket}
import actor.{ActorTest, SendingActor}
import akka.actor.Props
import akka.testkit.TestActorRef

class SendingActorTest extends ActorTest {
  "A Sending Actor" should "send a message to an actor when it has finished" in {
    val sendingActorRef = TestActorRef(Props[SendingActor], "sendingActor")
    val game = Game("Lakers vs. Bulls", Vector(Ticket(1), Ticket(2), Ticket(3)))
    sendingActorRef ! game
    expectMsgPF() { case Game(_, tickets) => tickets.size should be(game.tickets.size - 1) }
  }
}
