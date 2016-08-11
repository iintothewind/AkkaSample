package actor

import akka.actor._
import actor.Follower.FindActor
import actor.MyActor.{Greetings, Goodbye}

import scala.concurrent.Await
import scala.concurrent.duration._

class MyActor extends Actor with ActorLogging {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = log.info(s"$self is started")


  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = log.info(s"$self is stopped")

  override def receive: Receive = {
    case Greetings(greeter) => log.info(s"receive greetings from {}", greeter)
    case Goodbye() => log.info("Goodbye")
    case _ => log.info("received unknown messages")
  }
}

object MyActor {
  // provide factory method for Props in companion obj
  def props(): Props = Props(classOf[MyActor])

  //declare what messages an Actor can receive in companion obj
  sealed case class Greetings(from: String)

  sealed case class Goodbye()

}


object MyActors extends App {
  // ActorSystem is a heavy object: create only one per application
  val system = ActorSystem("HelloApp")
  // the name of ActorRef shoudl not be empty or start with $
  // InvalidActorNameException is thrown if the given name is already in use by another child to the same parent
  val myActor = system.actorOf(MyActor.props(), "myActor")
  val echoActor = system.actorOf(EchoActor.ref(), "echoActor")
  val follower = system.actorOf(Props(classOf[Follower]), "follower")
  follower ! FindActor(echoActor.path)
  myActor ! Greetings("Ivar")
  myActor.tell(Greetings("noSender"), Actor.noSender)
  myActor ! "1234"
  myActor.tell(Greetings("echoer"), echoActor)
  // kill the actor
  myActor ! PoisonPill
  // myActor has been killed, no more Greetings can be processed
  myActor.tell(Greetings("Ivar"), Actor.noSender)
  Await.result(system.terminate(), 10.seconds)
}
