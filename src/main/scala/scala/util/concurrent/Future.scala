package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[A] extends (() => Either[Throwable, A]) {
  def cancel(mayInterruptIfRunning: Boolean)

  def get(): Either[Throwable, A]

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A]

  def isCancelled: Boolean

  def isDone: Boolean

  override def apply() = get()

  def apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds) =
    get(timeout, unit)
}