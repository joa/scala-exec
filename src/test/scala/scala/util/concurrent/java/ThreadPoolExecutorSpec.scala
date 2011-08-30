package scala.util.concurrent.java

import org.specs2.mutable._

import scala.util.concurrent.TimeUnits

/**
 * @author Joa Ebert
 */
class ThreadPoolExecutorSpec extends Specification {
  private def newTPE() =
    new ThreadPoolExecutor(1, 1, Long.MaxValue, TimeUnits.Nanoseconds)

  private def withTPE[A](f: ThreadPoolExecutor => A) = {
    import scala.util.concurrent.DSL._

    using(newTPE())(f)
  }

  "A ThreadPoolExecutor" should {
    "execute submitted tasks" in {
      withTPE {
        exec =>
          val f = exec(() => true)
          f(100L) must beTrue
      }
    }

    "allow a return statement in submitted tasks" in {
      def task(): Boolean = {
        while(true) {
          return true
        }

        false
      }

      withTPE {
        exec =>
          val f = exec(task)
          f(100L) must beTrue
      }
    }

    "return the the closure for pending tasks" in {
      import _root_.java.util.concurrent.{LinkedBlockingQueue => JLinkedBlockingQueue}

      val exec = new ThreadPoolExecutor(
        1, 1, Long.MaxValue, TimeUnits.Nanoseconds,
        workQueue = new JLinkedBlockingQueue(2)
      )

      try {
        val task =
          () => {
            Thread.sleep(1000L)
            42
          }

        exec(() => Thread.sleep(1000L))
        exec(() => Thread.sleep(1000L))
        exec(task)

        val pending = exec.shutdownAndGetPending()

        pending must contain(task)
      } finally {
        exec.terminate()
      }
    }
  }
}
