package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[+A] extends (() => A) {
  original =>

  /**
   * Cancels the computation of this Future.
   */
  def cancel(mayInterruptIfRunning: Boolean)

  /**
   * Awaits the result of the Future.
   */
  def get(): Either[Throwable, A]

  /**
   * Awaits the result of the Future.
   *
   * Returns a Left(TimeoutException) if the Future could not be computed in
   * the given amount of time.
   */
  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A]

  /**
   * Whether or not the Future has been canceled.
   */
  def isCancelled: Boolean

  /**
   * Whether or not the Future has been computed.
   */
  def isDone: Boolean

  /**
   * Returns the value of the Future or throws an exception that might have occurred during its computation.
   */
  override def apply(): A = get().fold(throw _, identity)

  /**
   * Returns the value of the Future or throws an exception that might have occurred during its computation.
   */
  def apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds): A =
    get(timeout, unit).fold(throw _, identity)

  /**
   * The value of the Future if already computed.
   *
   * None is also returned if computing the Future failed for whatever reason.
   */
  def value: Option[A] =
    if(isDone) {
      get().right.toOption
    } else {
      None
    }

  /**
   * Returns a view on this Future.
   */
  def view = new FutureView[A] {
    override def cancel(mayInterruptIfRunning: Boolean) {
      original.cancel(mayInterruptIfRunning)
    }

    override def get(): Either[Throwable, A] = original.get()

    override def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
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

      override def get(): Either[Throwable, B] = applyMap(original.get())

      override def get(timeout: Long, unit: TimeUnit): Either[Throwable, B] =
        applyMap(original.get(timeout, unit))

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone

      private def applyMap(value: Either[Throwable, A]): Either[Throwable, B] =
        value match {
          case Left(x) => Left[Throwable, B](x)
          case Right(x) => Right(f(x))
        }
    }

  def flatMap[AA >: A, B](f: AA => Future[B]): Future[B] =
    new Future[B] {
      override def cancel(mayInterruptIfRunning: Boolean) {
        original.cancel(mayInterruptIfRunning)
      }

      override def get(): Either[Throwable, B] = applyFlatMap(original.get()).get()

      override def get(timeout: Long, unit: TimeUnit): Either[Throwable, B] =
        applyFlatMap(original.get(timeout, unit)).get(timeout, unit)

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone

      private def applyFlatMap(value: Either[Throwable, A]): Future[B] =
        value match {
          case Left(x) => new ErrorPreservingFuture(x)
          case Right(x) => f(x)
        }
    }

  def foreach[AA >: A, B](f: AA => B) {
    f(apply())
  }
}

private final class ErrorPreservingFuture[+A](error: Throwable) extends Future[A] {
  def cancel(mayInterruptIfRunning: Boolean) {}

  def get(): Either[Throwable, A] = Left(error)

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
    Left(error)

  def isCancelled: Boolean = false

  def isDone: Boolean = true
}