package actor

import akka.actor.{Terminated, Props, Actor}


class WatchActor extends Actor {
  val watchActorChild = context.actorOf(Props.empty, "child")
  context.watch(watchActorChild)

  override def receive: Receive = {
    case "kill" => context.stop(watchActorChild)
    case Terminated(actor) => println(s"child actor: $actor has been terminated")
  }
}
