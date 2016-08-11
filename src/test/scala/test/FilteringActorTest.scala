package test

import actor.FilteringActor.Event
import actor.{ActorTest, FilteringActor}
import akka.actor.Props
import akka.testkit.TestActorRef

class FilteringActorTest extends ActorTest {
  "A FilteringActor" should "be able to filter out particular messages" in {
    val filteringActor = TestActorRef(Props(classOf[FilteringActor], 5), "filteringActor")
    filteringActor ! Event(1)
    filteringActor ! Event(2)
    filteringActor ! Event(1)
    filteringActor ! Event(3)
    filteringActor ! Event(4)
    filteringActor ! Event(5)
    filteringActor ! Event(5)
    filteringActor ! Event(6)
    val eventIds = receiveWhile() { case Event(id) if id <= 5 => id }
    eventIds should be(1.to(5).toList)
    expectMsg(Event(6))
  }

  it should "be able to filter out the unexpected message" in {
    val filteringActor = TestActorRef(Props(classOf[FilteringActor], 5), "filteringActor")
    filteringActor ! Event(1)
    filteringActor ! Event(2)
    expectMsg(Event(1))
    expectMsg(Event(2))
    filteringActor ! Event(1)
    expectNoMsg()
    filteringActor ! Event(3)
    expectMsg(Event(3))
    filteringActor ! Event(1)
    expectNoMsg()
    filteringActor ! Event(4)
    filteringActor ! Event(5)
    filteringActor ! Event(5)
    expectMsg(Event(4))
    expectMsg(Event(5))
    expectNoMsg()
  }

  "A List" should "do sliding, filtering" in {
    val timeList = List(1, 2, 3, 5, 7, 9)
    timeList.sliding(2, 1).filter(e => e(1) - e.head <= 1).foreach(println)
    timeList.groupBy(_ % 2 == 0).foreach(println)
  }

}
