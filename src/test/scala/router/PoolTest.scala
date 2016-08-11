package router

import actor.EchoActor.Echo
import actor.{EchoActor, ActorTest}
import akka.actor.{Props, ActorSystem}
import akka.routing.{RoundRobinPool, Broadcast, FromConfig}
import com.typesafe.config.ConfigFactory

class PoolTest(_system: ActorSystem) extends ActorTest(_system) {
  def this() = this(ActorSystem("test", ConfigFactory.parseResources("router/routers.conf")))

  "A round robin router" should "be created from config file" in {
    val roundRobinRouterRef = system.actorOf(FromConfig.props(Props[Worker]), "round-robin-router")
    roundRobinRouterRef ! "001"
    roundRobinRouterRef ! "002"
    roundRobinRouterRef ! "003"
    roundRobinRouterRef ! "004"
    roundRobinRouterRef ! "005"
    roundRobinRouterRef ! Broadcast("Watch out, this message is for all routees.")

    val echoMsgs = receiveWhile() {
      case msg: String => msg
    }
    echoMsgs.foreach(println)
  }

  it should "also be created programmatically instead of configuration" in {
    val roundRobinRouterRef = system.actorOf(RoundRobinPool(5).props(Props[Worker]), "round-robin-router")
    roundRobinRouterRef ! "001"
    roundRobinRouterRef ! "002"
    roundRobinRouterRef ! "003"
    roundRobinRouterRef ! "004"
    roundRobinRouterRef ! "005"
    roundRobinRouterRef ! Broadcast("Watch out, this message is for all routees.")

    val echoMsgs = receiveWhile() {
      case msg: String => msg
    }
    echoMsgs.foreach(println)
  }

}
