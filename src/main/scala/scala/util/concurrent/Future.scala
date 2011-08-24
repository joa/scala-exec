package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[A] {
  def cancel(mayInterruptIfRunning: Boolean)

  def get(): Either[Throwable, A]

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A]

  def isCancelled: Boolean

  def isDone: Boolean
}