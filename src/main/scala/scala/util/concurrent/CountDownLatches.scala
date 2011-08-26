package scala.util.concurrent

/**
 * @author Joa Ebert
 */
object CountDownLatches {
  import java.CountDownLatchWrapper
  import _root_.java.util.concurrent.{CountDownLatch => JCountDownLatch}

  def create(count: Int): CountDownLatch =
    new CountDownLatchWrapper(new JCountDownLatch(count))
}