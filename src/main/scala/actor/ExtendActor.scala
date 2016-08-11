package actor

import akka.actor.{Actor, ActorRef, ActorLogging}

sealed case class GiveMeThings()

sealed case class Give(thing: Any)

trait ProducerBehavior {
  this: Actor =>
  val producerBehavior: Receive = {
    case GiveMeThings =>
      sender() ! Give("apple")
  }
}

trait ConsumerBehavior {
  this: Actor with ActorLogging =>
  val consumerBehavior: Receive = {
    case ref: ActorRef => ref ! GiveMeThings
    case Give(thing) => log.info("Got a thing! It's a(n) {}", thing)
  }
}

class Producer extends Actor with ProducerBehavior {
  override def receive: Actor.Receive = producerBehavior
}

class Consumer extends Actor with ActorLogging with ConsumerBehavior {
  override def receive: Actor.Receive = consumerBehavior
}

class ProducerConsumer extends Actor with ActorLogging with ProducerBehavior with ConsumerBehavior {
  override def receive: Actor.Receive = producerBehavior.orElse(consumerBehavior)
}
