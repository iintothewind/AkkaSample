package dispatcher

import scala.concurrent.duration._
import akka.actor.{ReceiveTimeout, Actor, ActorLogging}

class TimeoutActor extends Actor with ActorLogging {

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    context.setReceiveTimeout(100.milliseconds)
  }

  override def receive: Receive = {
    case msg: String => log.info(s"message: $msg, processed by: ${self.path}")
  }
}
