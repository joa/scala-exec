package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[+A] extends (() => Either[Throwable, A]) {
  def cancel(mayInterruptIfRunning: Boolean)

  def get(): Either[Throwable, A]

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A]

  def isCancelled: Boolean

  def isDone: Boolean

  override def apply() = get()

  def apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds) =
    get(timeout, unit)

  def asOption() = get().right.toOption

  def asOption(timeout: Long, unit: TimeUnit) = get(timeout, unit).right.toOption

  def asSeq() =
    get() match {
      case Left(_) => Seq.empty
      case Right(value) => Seq(value)
    }

  def asSeq(timeout: Long, unit: TimeUnit) =
    get(timeout, unit) match {
      case Left(_) => Seq.empty
      case Right(value) => Seq(value)
    }
}