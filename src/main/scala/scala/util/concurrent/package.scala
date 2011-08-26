package scala.util

/**
 * @author Joa Ebert
 */
package object concurrent {
  type TimeoutException = _root_.java.util.concurrent.TimeoutException
  type CancellationException = _root_.java.util.concurrent.CancellationException
  type InterruptedException = _root_.java.lang.InterruptedException

  type ThreadPoolExecutor = java.ThreadPoolExecutor
  val ThreadPoolExecutor = java.ThreadPoolExecutor

  def fork[U](f: => U) = {
    import _root_.java.lang.{Thread => JThread, Runnable => JRunnable}

    val result =
      new JThread(new JRunnable {
        override def run() {
          f
        }
      })

    result.start()
    result
  }
}