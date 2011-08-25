package scala.util.concurrent.java

import java.util.concurrent.{Semaphore => JSemaphore}
import scala.util.concurrent.{TimeUnit, Semaphore}

/**
 * @author Joa Ebert
 */
final class SemaphoreWrapper(semaphore: JSemaphore) extends Semaphore {
  override def acquire(permits: Int = 1) {
    semaphore.acquire(permits)
  }

  override def acquireUninterruptibly(permits: Int = 1) {
    semaphore.acquireUninterruptibly(permits)
  }

  override def tryAcquire(permits: Int = 1): Boolean =
    semaphore.tryAcquire(permits)

  override def tryAcquire(permits: Int, timeout: Long, unit: TimeUnit): Boolean =
    semaphore.tryAcquire(permits,
      JavaConversions.asJavaTimeUnit(timeout, unit),
      JavaConversions.JavaTimeUnit)

  override def release(permits: Int = 1) {
    semaphore.release(permits)
  }

  override def availablePermits: Int = semaphore.availablePermits

  override def drainPermits(): Int = semaphore.drainPermits()

  override def isFair: Boolean = semaphore.isFair

  override def hasQueuedThreads: Boolean = semaphore.hasQueuedThreads

  override def queueLength: Int = semaphore.getQueueLength
}