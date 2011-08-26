package scala.util.concurrent

/**
 * @author Joa Ebert
 */
object Semaphores {
  import _root_.java.util.concurrent.{Semaphore => JSemaphore}
  import java.SemaphoreWrapper

  def create(permits: Int = 1, fair: Boolean = false): Semaphore =
    new SemaphoreWrapper(new JSemaphore(permits, fair))
}