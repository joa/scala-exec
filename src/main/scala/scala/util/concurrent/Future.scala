package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[+A] extends (() => A) {
  original =>

  def cancel(mayInterruptIfRunning: Boolean)

  def get(): A

  def get(timeout: Long, unit: TimeUnit): A

  def isCancelled: Boolean

  def isDone: Boolean

  override def apply() = get()

  def apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds) =
    get(timeout, unit)

  def view = new FutureView[A] {
    override def cancel(mayInterruptIfRunning: Boolean) {
      original.cancel(mayInterruptIfRunning)
    }

    override def get(): A = original.get()

    override def get(timeout: Long, unit: TimeUnit): A =
      original.get(timeout, unit)

    override def isCancelled: Boolean = original.isCancelled

    override def isDone: Boolean = original.isDone
  }
}

trait FutureView[+A] extends Future[A] {
  original =>

  def map[AA >: A, B](f: AA => B): Future[B] =
    new Future[B] {
      override def cancel(mayInterruptIfRunning: Boolean) {
        original.cancel(mayInterruptIfRunning)
      }

      override def get(): B = f(original.get())

      override def get(timeout: Long, unit: TimeUnit): B =
        f(original.get(timeout, unit))

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone
    }

  def flatMap[AA >: A, B](f: AA => Future[B]): Future[B] =
    new Future[B] {
      override def cancel(mayInterruptIfRunning: Boolean) {
        original.cancel(mayInterruptIfRunning)
      }

      override def get(): B = f(original.get()).get()

      override def get(timeout: Long, unit: TimeUnit): B =
        f(original.get(timeout, unit)).get(timeout, unit)

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone
    }

  def foreach[AA >: A, B](f: AA => B) {
    f(get())
  }
}