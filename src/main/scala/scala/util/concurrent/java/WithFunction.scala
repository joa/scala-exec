package scala.util.concurrent.java

/**
 * @author Joa Ebert
 */
trait WithFunction[A] {
  def f: () => A
}