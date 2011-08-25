package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Semaphore {
  def acquire(permits: Int = 1)

  def acquireUninterruptibly(permits: Int = 1)

  def tryAcquire(permits: Int = 1): Boolean

  def tryAcquire(permits: Int, timeout: Long, unit: TimeUnit): Boolean

  def release(permits: Int = 1)

  def availablePermits: Int

  def drainPermits(): Int

  def isFair: Boolean

  def hasQueuedThreads: Boolean

  def queueLength: Int

  def withPermit[A](f: => A): A = withPermits(1)(f)

  def withPermits[A](permits: Int)(f: => A): A =
    try {
      acquire(permits)
      f
    } finally {
      release(permits)
    }
}