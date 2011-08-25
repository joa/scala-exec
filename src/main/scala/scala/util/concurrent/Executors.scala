package scala.util.concurrent

/**
 * @author Joa Ebert
 */
object Executors {
  //Java specific stuff leaks into this class but if we want to
  //keep it simple it currently has to be this way.
  
  import scala.util.concurrent.java._

  import _root_.java.util.concurrent.{
    Executor => JExecutor,
    Executors => JExecutors,
    ExecutorService => JExecutorService,
    ThreadPoolExecutor => JThreadPoolExecutor
  }
  
  import _root_.java.lang.{Runnable => JRunnable}

  def newCachedThreadPool(): ExecutorService =
    wrap(JExecutors.newCachedThreadPool())

  def newCachedThreadPool(threadFactory: JRunnable => Thread): ExecutorService =
    wrap(JExecutors.newCachedThreadPool(
      JavaConversions.asJavaThreadFactory(threadFactory)))

  def newFixedThreadPool(numThreads: Int): ExecutorService =
    wrap(JExecutors.newFixedThreadPool(numThreads))

  def newFixedThreadPool(numThreads: Int, threadFactory: JRunnable => Thread): ExecutorService =
    wrap(JExecutors.newFixedThreadPool(numThreads,
      JavaConversions.asJavaThreadFactory(threadFactory)))

  def newSingleThreadExecutor(): ExecutorService =
    wrap(JExecutors.newSingleThreadExecutor())

  def newSingleThreadExecutor(threadFactory: JRunnable => Thread): ExecutorService =
    wrap(JExecutors.newSingleThreadExecutor(
      JavaConversions.asJavaThreadFactory(threadFactory)))

  def defaultThreadFactory(): JRunnable => Thread = new Thread(_)

  private def wrap(executor: JExecutorService): ExecutorService =
    executor match {
      case threadPoolExecutor: JThreadPoolExecutor =>
        new ThreadPoolExecutor(threadPoolExecutor)
      case executorService: JExecutorService =>
        new ExecutorServiceWrapper(executorService)
    }

  private def wrap(executor: JExecutor): Executor =
    executor match {
      case threadPoolExecutor: JThreadPoolExecutor =>
        new ThreadPoolExecutor(threadPoolExecutor)
      case executorService: JExecutorService =>
        new ExecutorServiceWrapper(executorService)
      case executor: JExecutor =>
        new ExecutorWrapper(executor)
    }
}