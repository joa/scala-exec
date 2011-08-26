package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait CountDownLatch extends Ordered[CountDownLatch] {
  def await()

  def await(timeout: Long, unit: TimeUnit): Boolean

  def countDown()

  def count: Long

  def -=(value: Int) {
    synchronized {
      var i = 0
      
      while(i < value) {
        countDown()
        i += 1
      }
    }
  }

  final override def compare(that: CountDownLatch) =
    this.count compare that.count
}