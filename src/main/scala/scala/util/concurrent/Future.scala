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

  def map[B](f: A => B): Option[B] = asOption map f

  def flatMap[AA >: A, B](f: A => Option[AA]): Option[AA] =
    get() match {
      case Left(_) => None
      case Right(value) => f(value)
    }

  def filter(f: A => Boolean): Option[A] = asOption filter f

  def forall(f: A => Boolean) = asOption forall f

  def foreach[U](f: A => U) = asOption foreach f

  def toOption = asOption

  def toSeq = get() match {
    case Left(_) => Seq.empty
    case Right(value) => Seq(value)
  }

  private lazy val asOption = get().right.toOption
}