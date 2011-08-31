package scala.util.concurrent.java

import java.util.concurrent.{Future => JFuture}
import scala.util.concurrent.{TimeUnit, Future}

/**
 * @author Joa Ebert
 */
final class FutureWrapper[A](future: JFuture[A]) extends Future[A] {
  override def cancel(mayInterruptIfRunning: Boolean) {
    future.cancel(mayInterruptIfRunning)
  }

  override def get(): Either[Throwable, A] =
    try {
      Right(future.get())
    } catch {
      case x => Left(x)
    }

  override def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
    try {
      Right(future.get(
        JavaConversions.asJavaTimeUnit(timeout, unit),
        JavaConversions.JavaTimeUnit))
    } catch {
      case x => Left(x)
    }

  override def isCancelled: Boolean = future.isCancelled

  override def isDone: Boolean = future.isDone
}