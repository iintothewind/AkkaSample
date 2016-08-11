package dispatcher

import actor.{Follower, EchoActor, MyActor, ActorTest}
import akka.actor.{Props, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.testkit.TestActorRef
import com.typesafe.config.{ConfigValue, ConfigFactory}
import scala.collection.JavaConversions._

/**
There are 3 different types of message dispatchers:

Dispatcher
This is an event-based dispatcher that binds a set of Actors to a thread pool. It is the default dispatcher used if one is not specified.
Sharability: Unlimited
Mailboxes: Any, creates one per Actor
Use cases: Default dispatcher, Bulkheading
Driven by: java.util.concurrent.ExecutorService
specify using "executor" using "fork-join-executor", "thread-pool-executor" or the FQCN of an akka.dispatcher.ExecutorServiceConfigurator

PinnedDispatcher
This dispatcher dedicates a unique thread for each actor using it; i.e. each actor will have its own thread pool with only one thread in the pool.
Sharability: None
Mailboxes: Any, creates one per Actor
Use cases: Bulkheading
Driven by: Any akka.dispatch.ThreadPoolExecutorConfigurator
by default a "thread-pool-executor"
Note that thread-pool-executor configuration as per the above my-thread-pool-dispatcher example is NOT applicable.
This is because every actor will have its own thread pool when using PinnedDispatcher, and that pool will have only one thread.
Note that it's not guaranteed that the same thread is used over time,
since the core pool timeout is used for PinnedDispatcher to keep resource usage down in case of idle actors.
To use the same thread all the time you need to add thread-pool-executor.allow-core-timeout=off to the configuration of the PinnedDispatcher.

BalancingDispatcher, Deprecated, using BalancingPool router instead
This is an executor based event driven dispatcher that will try to redistribute work from busy actors to idle actors.
All the actors share a single Mailbox that they get their messages from.
It is assumed that all actors using the same instance of this dispatcher can process all messages that have been sent to one of the actors; i.e. the actors belong to a pool of actors, and to the client there is no guarantee about which actor instance actually processes a given message.
Sharability: Actors of the same type only
Mailboxes: Any, creates one for all Actors
Use cases: Work-sharing
Driven by: java.util.concurrent.ExecutorService
specify using "executor" using "fork-join-executor", "thread-pool-executor" or the FQCN of an akka.dispatcher.ExecutorServiceConfigurator
Note that you can not use a BalancingDispatcher as a Router Dispatcher. (You can however use it for the Routees)

CallingThreadDispatcher
This dispatcher runs invocations on the current thread only. This dispatcher does not create any new threads, but it can be used from different threads concurrently for the same actor. See CallingThreadDispatcher for details and restrictions.
Sharability: Unlimited
Mailboxes: Any, creates one per Actor per Thread (on demand)
Use cases: Testing
Driven by: The calling thread (duh)
http://doc.akka.io/docs/akka/2.4.3/scala/testing.html#scala-callingthreaddispatcher
  */
class SettingDispatcherTest(_system: ActorSystem) extends ActorTest(_system) {
  def this() = this(ActorSystem("akka", ConfigFactory.parseResources("dispatcher/dispatchers.conf")))

  "system config" should "be printed" in {
    system.settings.config.entrySet().filter {
      case e: java.util.Map.Entry[String, ConfigValue] => e.getKey.contains("akka.fork-join-dispatcher") || e.getKey.contains("akka.thread-pool-dispatcher")
    }.foreach(println(_))
    assert(system.dispatchers.hasDispatcher("akka.fork-join-dispatcher"))
    assert(system.dispatchers.hasDispatcher("akka.thread-pool-dispatcher"))
  }

  "fork-join-dispatcher" should "be looked up" in {
    //Dispatchers implement the ExecutionContext interface and can thus be used to run Future invocations etc.
    val executionContext: MessageDispatcher = system.dispatchers.lookup("akka.fork-join-dispatcher")
    println(executionContext.configurator.dispatcher())
  }

  "thread-pool-dispatcher" should "be looked up" in {
    val executionContext: MessageDispatcher = system.dispatchers.lookup("akka.thread-pool-dispatcher")
    println(executionContext.configurator.dispatcher())
  }

  "An actor" should "be created by specifying dispatcher" in {
    // define dispatcher in deployment configuration in .conf file wont work for TestActorRef
    //val myActorRef = system.actorOf(Props[MyActor], "myActor")
    // pragmatically define dispatcher for actor
    val myActorRef = TestActorRef(Props[MyActor].withDispatcher("akka.fork-join-dispatcher"), "myActor")
    val echoActorRef = TestActorRef(Props[EchoActor].withDispatcher("akka.thread-pool-dispatcher"), "echoActor")
    // core-pool-size-min, core-pool-size-max are not applicable for thread-pool-executor in PinnedDispatcher type,
    // This is because every actor will have its own thread pool when using PinnedDispatcher, and that pool will have only one thread.
    val followerRef = TestActorRef(Props[Follower].withDispatcher("akka.pinned-dispatcher"), "follower")
    myActorRef.dispatcher.id shouldBe "akka.fork-join-dispatcher"
    echoActorRef.dispatcher.id shouldBe "akka.thread-pool-dispatcher"
    followerRef.dispatcher.id shouldBe "akka.pinned-dispatcher"
  }

}
