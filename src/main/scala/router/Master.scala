package router

import actor.EchoActor
import akka.actor.{Terminated, Props, ActorLogging, Actor}
import akka.routing.{Broadcast, Router, RoundRobinRoutingLogic, ActorRefRoutee}

class Master extends Actor with ActorLogging {
  var router = {
    val routees = Vector.fill(5) {
      val routeeRef = context.actorOf(Props[Worker])
      context.watch(routeeRef)
      ActorRefRoutee(routeeRef)
    }

    /*The routing logic shipped with Akka are:
      akka.routing.RoundRobinRoutingLogic
      akka.routing.RandomRoutingLogic
      akka.routing.SmallestMailboxRoutingLogic
      akka.routing.BroadcastRoutingLogic
      akka.routing.ScatterGatherFirstCompletedRoutingLogic
      akka.routing.TailChoppingRoutingLogic
      akka.routing.ConsistentHashingRoutingLogic
     */
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Receive = {
    case msg: String =>
      router.route(msg, sender())
    case pm@ParentMessage(msg) =>
      router.route(pm, sender())
    case broadcast@Broadcast(msg) =>
      router.route(broadcast, sender())
    case Terminated(routee) =>
      router = router.removeRoutee(routee)
      val routeeRef = context.actorOf(Props[EchoActor])
      context.watch(routeeRef)
      router = router.addRoutee(routeeRef)
  }
}
