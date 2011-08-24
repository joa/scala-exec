package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Delayed extends Ordered[Delayed] {
  def delay: Long

  final override def compare(that: Delayed) =
    this.delay compare that.delay
}