package scala.util.concurrent.java

import scala.util.concurrent.ExecutorService
import scala.util.concurrent.{
  Days, Hours, Microseconds, Milliseconds,
  Minutes, Nanoseconds, Seconds, TimeUnit}
import scala.util.concurrent.Future

import java.util.concurrent.{
  TimeUnit => JTimeUnit,
  ThreadPoolExecutor => JThreadPoolExecutor}

/**
 * @author Joa Ebert
 */
class ThreadPoolExecutor(private val threadPoolExecutor: JThreadPoolExecutor)
    extends ExecutorService {
  import scala.collection.JavaConversions._

  override def execute[A](f: () => A) {
    threadPoolExecutor.execute(JavaConversions.asJavaRunnable(f))
  }
  
  override def awaitTermination(timeout: Long, unit: TimeUnit): Boolean =
    threadPoolExecutor.
      awaitTermination(JavaConversions.
        asJavaTimeUnit(timeout, unit), JavaConversions.JavaTimeUnit)

  override def invokeAll[A](collection: Iterable[() => A]): Seq[Future[A]] =
    threadPoolExecutor.
      invokeAll(
        collection map JavaConversions.asJavaCallable
      ) map JavaConversions.asScalaFuture

  override def invokeAll[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): Seq[Future[A]] =
    threadPoolExecutor.
      invokeAll(
        collection map JavaConversions.asJavaCallable,
        JavaConversions.asJavaTimeUnit(timeout, unit),
        JavaConversions.JavaTimeUnit
      ) map JavaConversions.asScalaFuture

  override def invokeAny[A](collection: Iterable[() => A]): A =
    threadPoolExecutor.
        invokeAny(collection map JavaConversions.asJavaCallable)

  override def invokeAny[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): A =
    threadPoolExecutor.
        invokeAny(collection map JavaConversions.asJavaCallable,
        JavaConversions.asJavaTimeUnit(timeout, unit),
        JavaConversions.JavaTimeUnit)

  override def isShutdown: Boolean =
    threadPoolExecutor.isShutdown()

  override def isTerminated: Boolean =
    threadPoolExecutor.isTerminated()

  def isTerminating =
    threadPoolExecutor.isTerminating()

  override def shutdown() {
    threadPoolExecutor.shutdown()
  }

  override def shutdownNow(): Seq[() => _] =
    (threadPoolExecutor.shutdownNow() collect {
      case x: WithFunction[_] => x.f
    }).toSeq

  override def submit[A](f: () => A): Future[A] =
    JavaConversions.asScalaFuture(
      threadPoolExecutor.
          submit(JavaConversions.asJavaCallable(f)))

  override def submit[A, B](f: () => A, result: B): Future[B] =
    JavaConversions.asScalaFuture(
      threadPoolExecutor.submit(JavaConversions.asJavaRunnable(f), result))

  def allowCoreThreadTimeOut = threadPoolExecutor.allowsCoreThreadTimeOut()

  def allowCoreThreadTimeout_=(value: Boolean) {
    threadPoolExecutor.allowCoreThreadTimeOut(value)
  }

  def activeCount = threadPoolExecutor.getActiveCount

  def completedTaskCount = threadPoolExecutor.getCompletedTaskCount

  def corePoolSize = threadPoolExecutor.getCorePoolSize

  def corePoolSize_=(value: Int) {
    threadPoolExecutor.setCorePoolSize(value)
  }

  def getKeepAliveTime(unit: TimeUnit) =
    threadPoolExecutor.getKeepAliveTime(unit match {
      case Nanoseconds => JTimeUnit.NANOSECONDS
      case Microseconds => JTimeUnit.MICROSECONDS
      case Milliseconds => JTimeUnit.MILLISECONDS
      case Seconds => JTimeUnit.SECONDS
      case Minutes => JTimeUnit.MINUTES
      case Hours => JTimeUnit.HOURS
      case Days => JTimeUnit.DAYS
    })

  def setKeepAliveTime(time: Long, unit: TimeUnit) {
    threadPoolExecutor.setKeepAliveTime(
      JavaConversions.asJavaTimeUnit(time, unit),
      JavaConversions.JavaTimeUnit)
  }

  def largestPoolSize = threadPoolExecutor.getLargestPoolSize

  def maximumPoolSize = threadPoolExecutor.getMaximumPoolSize

  def maximumPoolSize_=(value: Int) {
    threadPoolExecutor.setMaximumPoolSize(value)
  }

  def poolSize = threadPoolExecutor.getPoolSize

  //threadPoolExecutor.getRejectedExecutionHandler
  //threadPoolExecutor.setRejectedExecutionHandler

  //threadPoolExecutor.getQueue

  def taskCount = threadPoolExecutor.getTaskCount

  //threadPoolExecutor.getThreadFactory
  //threadPoolExecutor.setThreadFactory()

  def prestartAllCoreThreads() {
    threadPoolExecutor.prestartAllCoreThreads()
  }

  def prestartCoreThread() {
    threadPoolExecutor.prestartCoreThread()
  }

  def purge() {
    threadPoolExecutor.purge()
  }
}
