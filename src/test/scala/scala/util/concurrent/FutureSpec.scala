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
          f() must beTrue
      }
    }

    "return a TimeoutException on timeout" in {
      withExec {
        exec =>
          val f = exec(() => Thread.sleep(1000L))
          f(1L, TimeUnits.Nanoseconds) must throwA[TimeoutException]
      }
    }

    /*"support for-selector syntax" in {
      withExec {
        exec =>
          def task() = 1

          val tasks: List[() => Int] = List.fill(64) { task _ }

          val results: Seq[Int] =
            for {
              future <- exec.invokeAll(tasks)
              result <- future
            } yield {
              result
            }

          results.sum must_== (tasks map { _() }).sum
      }
    }

    "be None when cancelled" in {
      import DSL._

      using(Executors.newFixedThreadPool(2)) {
        exec =>
          val f0 =
            submit {
              () => {
                Thread.sleep(1000L)
                false
              }
            } to exec

          val f1 =
            submit {
              () => {
                f0.cancel(true)
              }
            } to exec

          f0.asOption() must beNone
      }
    }

    "be None on timeout" in {
      withExec {
        exec =>
          val f = exec(() => {
            Thread.sleep(1000L)
            false
          })

          f.asOption(1L, TimeUnits.Nanoseconds) must beNone
      }
    }

    "return the alternative when an exception occurrs" in {
      withExec {
        exec =>
          def task(): Boolean = sys.error("Expected.")
          val f = exec(task)

          f.asOption() must beNone
      }
    }*/
  }
}