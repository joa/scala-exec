package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait ScheduledFuture[A] extends Future[A] with Delayed