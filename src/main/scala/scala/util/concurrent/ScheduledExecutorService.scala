package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait ScheduledExecutorService extends ExecutorService {
  def schedule[A](f: => A, delay: Long, unit: TimeUnit): ScheduledFuture[A]

  def scheduleAtFixedRate[A](f: => A, initialDelay: Long, period: Long, unit: TimeUnit)

  def scheduleWithFixedDelay[A](f: => A, initialDelay: Long, period: Long, unit: TimeUnit)
}