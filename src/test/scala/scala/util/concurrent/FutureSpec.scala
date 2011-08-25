package scala.util.concurrent

import org.specs2.mutable._

/**
 * @author Joa Ebert
 */
class FutureSpec extends Specification {
  private def withExec[A](f: ExecutorService => A) = {
    import DSL._
    using(Executors.newSingleThreadExecutor())(f)
  }

  "The Future[A]" should {
    "block and return a result by default" in {
      withExec {
        exec =>
          val f = exec(() => true)
          f() must beRight(true)
      }
    }

    "return a TimeoutException on timeout" in {
      import _root_.java.util.concurrent.TimeoutException

      withExec {
        exec =>
          val f = exec(() => Thread.sleep(1000L))
          f(1L) must beLeft.like {
            case error => error must beAnInstanceOf[TimeoutException]
          }
      }
    }
  }
}