package fault

import akka.actor.SupervisorStrategy.{Stop, Restart}
import akka.actor._
import akka.event.LoggingReceive
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

object DummyDB {

  import Storage.StorageException

  private var db = Map[String, Long]()

  @throws(classOf[StorageException])
  def save(key: String, value: Long): Unit = synchronized {
    if (value >= 11 && value <= 14)
      throw new StorageException("Simulated store failure " + value)
    db += (key -> value)
  }

  @throws(classOf[StorageException])
  def load(key: String): Option[Long] = synchronized {
    db.get(key)
  }
}

object Storage {

  sealed case class Entry(key: String, value: Long)

  sealed case class Store(entry: Entry)

  sealed case class Get(key: String)

  class StorageException(msg: String) extends RuntimeException(msg: String)

}

class Storage extends Actor with ActorLogging {

  import Storage._

  val db = DummyDB

  override def receive: Actor.Receive = LoggingReceive {
    case Store(Entry(key, count)) =>
      log.debug("receiving: {}", Store(Entry(key, count)))
      db.save(key, count)
    case Get(key) =>
      log.debug("receiving: {}", Get(key))
      sender ! Entry(key, db.load(key).getOrElse(0L))
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    log.info(s"${getClass.getCanonicalName} started")
  }
}

class Listener extends Actor with ActorLogging {

  import Worker._

  context.setReceiveTimeout(5.seconds)

  override def receive: Receive = {
    case Progress(percent) =>
      log.info("Current progress: {}%", percent)
      if (percent >= 100.0) {
        log.info("That's all, shutting down")
        context.system.terminate()
      }
    case ReceiveTimeout =>
      log.error("Shutting down due to unavalible service")
      context.system.terminate()
  }
}


object Worker {

  sealed case class Start()

  sealed case class Do()

  sealed case class Progress(percent: Double)

}

class Worker extends Actor with ActorLogging {

  import Worker._
  import CounterService._
  import context.dispatcher
  import akka.pattern.{ask, pipe}

  var progressLister: Option[ActorRef] = None
  val counterService = context.actorOf(Props[CounterService], name = "counterService")
  val totalCount = 51
  implicit val askTimeount = Timeout(5.seconds)

  override val supervisorStrategy = OneForOneStrategy() {
    case _: CounterService.ServiceUnavailableException => Stop
  }

  override def receive: Actor.Receive = LoggingReceive {
    case Start if progressLister.isEmpty =>
      log.debug(s"start Worker by scheduling Do in each second")
      progressLister = Some(sender())
      context.system.scheduler.schedule(Duration.Zero, 1.second, self, Do)
    case Do =>
      counterService ! Increment(1)
      counterService ! Increment(1)
      counterService ! Increment(1)
      (counterService ? GetCurrentCount()).map {
        case CurrentCount(_, count) =>
          log.debug(s"receiving CurrentCount(_, {})", count)
          Progress(100.0 * count / totalCount)
      }.pipeTo(progressLister.get)
  }
}


object Counter {

  sealed case class UseStorage(storage: Option[ActorRef])

}

class Counter(key: String, initialValue: Long) extends Actor with ActorLogging {

  import Counter._
  import CounterService._
  import Storage._

  var count = initialValue
  var storage: Option[ActorRef] = None

  def storeCount(): Unit = {
    storage.foreach(_ ! Store(Entry(key, count)))
  }

  override def receive: Actor.Receive = LoggingReceive {
    case UseStorage(s) =>
      log.debug(s"receiving {}", UseStorage(s))
      storage = s
      storeCount()
    case Increment(n) =>
      log.debug(s"receiving {}", Increment(n))
      count += n
      storeCount()
    case msg: GetCurrentCount =>
      log.debug(s"receiving {}", msg)
      sender ! CurrentCount(key, count)
  }

}


object CounterService {

  sealed case class Increment(n: Int)

  sealed case class GetCurrentCount()

  sealed case class CurrentCount(key: String, count: Long)

  sealed case class Reconnect()


  class ServiceUnavailableException(msg: String) extends RuntimeException(msg)

}

class CounterService extends Actor with ActorLogging {

  import Counter._
  import Storage._
  import CounterService._
  import context.dispatcher

  val key = self.path.name
  var storage: Option[ActorRef] = None
  var counter: Option[ActorRef] = None
  var backlog = IndexedSeq.empty[(ActorRef, Any)]
  val MaxBacklog = 10000

  def initStorage(): Unit = {
    storage = Some(context.watch(context.actorOf(Props[Storage], name = "storage")))
    counter.foreach(_ ! UseStorage(storage))
    storage.get ! Get(key)
  }

  override def preStart(): Unit = {
    initStorage()
  }


  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 5.seconds) {
    case _: Storage.StorageException => Restart
  }

  def forwardOrPlaceInBacklog(msg: Any): Unit = {
    counter match {
      case Some(ref: ActorRef) => ref.forward(msg)
      case None =>
        if (backlog.size >= MaxBacklog)
          throw new ServiceUnavailableException("counterService not available, lack of initial value.")
        backlog :+= (sender -> msg)
    }
  }

  override def receive: Actor.Receive = LoggingReceive {
    case Entry(k, v) if k == key && counter.isEmpty =>
      log.info(s"receving {}", Entry(k, v))
      val c = context.actorOf(Props(classOf[Counter], key, v), "counter")
      counter = Some(c)
      c ! UseStorage(storage)
      backlog.foreach(msg => log.info(s"backlong message: {} -> {} ", msg._1, msg._2))
      for ((replyTo, msg) <- backlog) c.tell(msg, sender = replyTo)
      backlog = IndexedSeq.empty
    case msg: Increment =>
      log.debug(s"receiving {}", msg)
      forwardOrPlaceInBacklog(msg)
    case msg: GetCurrentCount =>
      log.debug(s"receiving {}", msg)
      forwardOrPlaceInBacklog(msg)
    case Terminated(actorRef) if storage.contains(actorRef) =>
      log.debug(s"receiving Terminated({})", actorRef)
      storage = None
      counter.foreach(_ ! UseStorage(None))
      context.system.scheduler.scheduleOnce(3.seconds, self, Reconnect)
    case Reconnect =>
      log.debug(s"receiving {}", Reconnect)
      initStorage()
  }
}

object FaultHandlingDocSample extends App {

  import Worker._

  val system = ActorSystem("FaultToleranceSample")
  val worker = system.actorOf(Props[Worker], name = "worker")
  val listener = system.actorOf(Props[Listener], name = "listener")
  worker.tell(Start, sender = listener)
}
