package scala.util.concurrent.java

import scala.collection.JavaConversions._
import scala.util.concurrent.{ExecutorService, Future, TimeUnit}
import java.util.concurrent.{
  AbstractExecutorService => JAbstractExecutorService,
  ExecutorService => JExecutorService,
  RunnableFuture => JRunnableFuture,
  TimeUnit => JTimeUnit,
  Callable => JCallable }
import java.lang.{Runnable => JRunnable}
/**
 * @author Joa Ebert
 */
class ExecutorServiceWrapper(executorService: JExecutorService)
    extends JAbstractExecutorService with ExecutorService {

  override def execute(f: JRunnable) {
    executorService.execute(f)
  }

  override def execute[A](f: () => A) {
    execute(JavaConversions.asJavaRunnable(f))
  }

  override def awaitTermination(timeout: Long, unit: JTimeUnit): Boolean =
    executorService.awaitTermination(timeout, unit)

  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    awaitTermination(JavaConversions.
      asJavaTimeUnit(timeout, unit), JavaConversions.JavaTimeUnit)

  override def invokeAll[A](collection: Iterable[() => A]): Seq[Future[A]] =
    invokeAll(
      collection map JavaConversions.asJavaCallable
    ) map JavaConversions.asScalaFuture

  override def invokeAll[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): Seq[Future[A]] =
    invokeAll(
      collection map JavaConversions.asJavaCallable,
      JavaConversions.asJavaTimeUnit(timeout, unit),
      JavaConversions.JavaTimeUnit
    ) map JavaConversions.asScalaFuture

  override def invokeAny[A](collection: Iterable[() => A]): A =
    invokeAny(collection map JavaConversions.asJavaCallable)

  override def invokeAny[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): A =
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

  override def shutdownNow() = executorService.shutdownNow()
  
  override def shutdownAndGetPending(): Seq[() => _] =
    (shutdownNow() collect {
      case x: WithFunction[_] => x.f
    }).toSeq

  override def submit[A](f: () => A): Future[A] =
    JavaConversions.asScalaFuture(
      submit(JavaConversions.asJavaCallable(f)))


  override def submit[A, B](f: () => A, result: B): Future[B] =
    JavaConversions.asScalaFuture(
      submit(JavaConversions.asJavaRunnable(f), result))

  //TODO can we do better? submit already uses JavaConversions.asJavaXXXXX
  
  protected def newTaskFor[A, B](f: () => A, result: B): JRunnableFuture[B] =
    JavaConversions.asJavaFutureTask(f, result)

  protected def newTaskFor[A](f: () => A): JRunnableFuture[A] =
    JavaConversions.asJavaFutureTask(f)

  override protected def newTaskFor[A](callable: JCallable[A]): JRunnableFuture[A] =
    callable match {
      case x: WithFunction[A] => newTaskFor(x.f)
      case other => super.newTaskFor(other)
    }
  
  override protected def newTaskFor[A](runnable: JRunnable, result: A): JRunnableFuture[A] =
    runnable match {
      case x: WithFunction[_] => newTaskFor(x.f, result)
      case other => super.newTaskFor(other, result)
    }
}