package scala.util.concurrent.java

import scala.util.concurrent.TimeUnit
import scala.util.concurrent.Future

import java.util.concurrent.{
  ThreadFactory => JThreadFactory,
  TimeUnit => JTimeUnit,
  Callable => JCallable,
  Future => JFuture,
  FutureTask => JFutureTask
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
  
  implicit def asScalaFuture[A](value: JFuture[A]): Future[A] = new FutureWrapper(value)

  implicit def asJavaRunnable[A](f: () => A): JRunnable = new Runnable[A](f)

  implicit def asJavaCallable[A](f: () => A): JCallable[A] = new Callable[A](f)

  implicit def asJavaFutureTask[A](f: () => A): JFutureTask[A] = new FutureTask[A](f)

  implicit def asJavaFutureTask[A, B](f: () => A, result: B): JFutureTask[B] = new FutureTaskWithResult[A, B](f, result)

  implicit def asJavaThreadFactory(f: JRunnable => Thread): JThreadFactory =
    new JThreadFactory {
      def newThread(runnable: JRunnable) = f(runnable)
    }
}