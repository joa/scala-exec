package scala.util.concurrent.java

import scala.collection.JavaConversions._
import scala.util.concurrent.{ExecutorService, Future, TimeUnit}
import java.util.concurrent.{ExecutorService => JExecutorService}

/**
 * @author Joa Ebert
 */
class ExecutorServiceWrapper(executorService: JExecutorService)
    extends ExecutorService {
  override def execute[A](f: () => A) {
    executorService.execute(JavaConversions.asJavaRunnable(f))
  }

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    executorService.
      awaitTermination(JavaConversions.
        asJavaTimeUnit(timeout, unit), JavaConversions.JavaTimeUnit)

  override def invokeAll[A](collection: Iterable[() => A]): Seq[Future[A]] =
    executorService.
      invokeAll(
        collection map JavaConversions.asJavaCallable
      ) map JavaConversions.asScalaFuture

  override def invokeAll[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): Seq[Future[A]] =
    executorService.
      invokeAll(
        collection map JavaConversions.asJavaCallable,
        JavaConversions.asJavaTimeUnit(timeout, unit),
        JavaConversions.JavaTimeUnit
      ) map JavaConversions.asScalaFuture

  override def invokeAny[A](collection: Iterable[() => A]): A =
    executorService.
        invokeAny(collection map JavaConversions.asJavaCallable)

  override def invokeAny[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): A =
    executorService.
        invokeAny(collection map JavaConversions.asJavaCallable,
        JavaConversions.asJavaTimeUnit(timeout, unit),
        JavaConversions.JavaTimeUnit)

  override def isShutdown: Boolean =
    executorService.isShutdown()

  override def isTerminated: Boolean =
    executorService.isTerminated()

  override def shutdown() {
    executorService.shutdown()
  }

  override def shutdownNow(): Seq[() => _] =
    (executorService.shutdownNow() collect {
      case x: WithFunction[_] => x.f
    }).toSeq

  override def submit[A](f: () => A): Future[A] =
    JavaConversions.asScalaFuture(
      executorService.
          submit(JavaConversions.asJavaCallable(f)))

  override def submit[A, B](f: () => A, result: B): Future[B] =
    JavaConversions.asScalaFuture(
      executorService.submit(JavaConversions.asJavaRunnable(f), result))
}