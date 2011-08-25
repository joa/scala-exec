package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait ExecutorService extends Executor {
  def awaitTermination(timeout: Long, unit: TimeUnit): Boolean

  def invokeAll[A](collection: Iterable[() => A]): Seq[Future[A]]

  def invokeAll[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): Seq[Future[A]]
  
  def invokeAny[A](collection: Iterable[() => A]): A

  def invokeAny[A](collection: Iterable[() => A], timeout: Long, unit: TimeUnit): A

  def isShutdown: Boolean

  def isTerminated: Boolean

  def shutdown()

  def shutdownNow(): Seq[() => _]

  def submit[A](f: () => A): Future[A]

  def submit[A, B](f: () => A, result: B): Future[B]

  /**
   * Terminates the executor service.
   */
  def terminate(timeout: Long = 500L, unit: TimeUnit = TimeUnits.Milliseconds) {
    shutdown()

    while(true) {
      try {
        awaitTermination(timeout, unit)
        return;
      } catch {
        case interrupt: InterruptedException =>
          // Try again.
      }
    }
  }
}