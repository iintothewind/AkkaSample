package actor

import akka.actor.{ActorLogging, Actor}

class ConsolePrintActor extends Actor {
  override def receive: Receive = {
    case msg: String => println(msg)
  }
}

class LogPrintActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: String => log.warning(s"msg = $msg")
  }
}
