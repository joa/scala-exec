package scala.util.concurrent.java

import scala.util.concurrent.TimeUnit
import scala.util.concurrent.Future

import java.util.concurrent.{
  ThreadFactory => JThreadFactory,
  TimeUnit => JTimeUnit,
  Callable => JCallable,
  Future => JFuture
}

import java.lang.{Runnable => JRunnable}

/**
 * @author Joa Ebert
 */
object JavaConversions {
  /**
   * Converts a given scala.util.concurrent.TimeUnit to
   * a Java TimeUnit.NANOSECONDS value.
   */
  def asJavaTimeUnit(value: Long, unit: TimeUnit): Long = unit.toNanos(value)

  val JavaTimeUnit = JTimeUnit.NANOSECONDS
  
  def asScalaFuture[A](value: JFuture[A]): Future[A] = new FutureWrapper(value)

  def asJavaRunnable[A](f: () => A): JRunnable = new Runnable[A](f)

  def asJavaCallable[A](f: () => A): JCallable[A] = new Callable[A](f)

  def asJavaThreadFactory(f: JRunnable => Thread): JThreadFactory =
    new JThreadFactory {
      def newThread(runnable: JRunnable) = f(runnable)
    }

  def asEither[A](f: => A): Either[Throwable, A] =
    try {
      Right(f)
    } catch {
      case throwable: Throwable => Left(throwable)
    }
}