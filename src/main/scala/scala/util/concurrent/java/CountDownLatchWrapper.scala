package scala.util.concurrent.java

import java.util.concurrent.{CountDownLatch => JCountDownLatch}
import scala.util.concurrent.{TimeUnit, CountDownLatch}

/**
 * @author Joa Ebert
 */
final class CountDownLatchWrapper(countDownLatch: JCountDownLatch) extends CountDownLatch {
  def await() {
    countDownLatch.await()
  }

  def await(timeout: Long, unit: TimeUnit): Boolean =
    countDownLatch.await(JavaConversions.asJavaTimeUnit(timeout, unit), JavaConversions.JavaTimeUnit)

  def countDown() {
    countDownLatch.countDown()
  }

  def count: Long = countDownLatch.getCount
}