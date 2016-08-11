package actor

import akka.actor.Props
import akka.testkit.TestActorRef


class ExtendTest extends ActorTest {
  "A Producer" should "be able to produce things" in {
    val producer = TestActorRef(Props(classOf[Producer]), "producer")
    producer ! GiveMeThings
    expectMsgType[Give].thing should be("apple")
  }

  "A Consumer" should "be able to consume things" in {
    val consumer = TestActorRef(Props(classOf[Consumer]), "consumer")
    consumer ! Give("star")
  }

  "A ProducerConsumer" should "be able to produce and consume" in {
    val producerConsumer = TestActorRef(Props(classOf[ProducerConsumer]), "producer-consumer")
    producerConsumer ! producerConsumer
  }

}
