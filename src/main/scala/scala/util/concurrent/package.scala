package scala.util.concurrent

/**
 * @author Joa Ebert
 */
package object concurrent {
  type TimeoutException = _root_.java.util.concurrent.TimeoutException
  type CancellationException = _root_.java.util.concurrent.CancellationException
  type InterruptedException = _root_.java.lang.InterruptedException

  type ThreadPoolExecutor = java.ThreadPoolExecutor
  val ThreadPoolExecutor = java.ThreadPoolExecutor
}