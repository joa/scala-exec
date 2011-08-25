package scala.util.concurrent.java

import scala.util.concurrent.{Executors, ExecutorService, Future, TimeUnit, TimeUnits}

import java.util.concurrent.{
  TimeUnit => JTimeUnit,
  ThreadPoolExecutor => JThreadPoolExecutor,
  BlockingQueue => JBlockingQueue,
  SynchronousQueue => JSynchronousQueue,
  ThreadFactory => JThreadFactory,
  RejectedExecutionHandler => JRejectedExecutionHandler,
  Executors => JExecutors}

import java.lang.{Runnable => JRunnable}

/**
 * @author Joa Ebert
 */
class ThreadPoolExecutor(threadPoolExecutor: JThreadPoolExecutor)
    extends ExecutorServiceWrapper(threadPoolExecutor) {
  import scala.collection.JavaConversions._

  def this(
      corePoolSize: Int,
      maximumPoolSize: Int,
      keepAliveTime: Long,
      unit: TimeUnit,
      workQueue: JBlockingQueue[JRunnable] = new JSynchronousQueue(),
      threadFactory: JRunnable => Thread = Executors.defaultThreadFactory(),
      handler: JRejectedExecutionHandler = new JThreadPoolExecutor.AbortPolicy()
  ) = this(
    new JThreadPoolExecutor(
      corePoolSize,
      maximumPoolSize,
      JavaConversions.asJavaTimeUnit(keepAliveTime, unit),
      JavaConversions.JavaTimeUnit,
      workQueue,
      JavaConversions.asJavaThreadFactory(threadFactory),
      handler
    )
  )

  def isTerminating =
    threadPoolExecutor.isTerminating()

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

  def getKeepAliveTime(unit: TimeUnit) = {
    import TimeUnits._
    
    threadPoolExecutor.getKeepAliveTime(unit match {
      case Nanoseconds => JTimeUnit.NANOSECONDS
      case Microseconds => JTimeUnit.MICROSECONDS
      case Milliseconds => JTimeUnit.MILLISECONDS
      case Seconds => JTimeUnit.SECONDS
      case Minutes => JTimeUnit.MINUTES
      case Hours => JTimeUnit.HOURS
      case Days => JTimeUnit.DAYS
    })
  }

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

  def rejectedExecutionHandler =
    threadPoolExecutor.getRejectedExecutionHandler

  def rejectedExecutionHandler_=(value: JRejectedExecutionHandler) {
    threadPoolExecutor.setRejectedExecutionHandler(value)
  }

  def queue = threadPoolExecutor.getQueue

  def taskCount = threadPoolExecutor.getTaskCount

  def threadFactory = threadPoolExecutor.getThreadFactory

  def threadFactory_=(value: JThreadFactory) {
    threadPoolExecutor.setThreadFactory(value)
  }

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
