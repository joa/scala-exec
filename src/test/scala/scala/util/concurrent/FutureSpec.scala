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

    "support for-selector syntax" in {
      withExec {
        exec =>
          def task() = 1

          val tasks: List[() => Int] = List.fill(64) { task _ }

          val results: Seq[Int] =
            for {
              future <- exec.invokeAll(tasks)
              result <- future.get().right.toOption //TODO fix me
            } yield {
              result
            }

          results.sum must_== (tasks map { _() }).sum
      }
    }

    "return the error for any of two joined Futures" in {
      withExec {
        exec =>
          val f0 = exec(() => { sys.error("Error"); 1 })
          val f1 = exec(() => 2)
          val f2 = f0 join f1

          f2.get() must beLeft
      }

      withExec {
        exec =>
          val f0 = exec(() => 1)
          val f1 = exec(() => { sys.error("Error"); 2 })
          val f2 = f0 join f1
          f2.get() must beLeft
      }
    }

    "try also the second option when the first Future fails in an OR case" in {
      withExec {
        exec =>
          val f0 = exec(() => { Thread.sleep(32L); 1 })
          val f1 = exec(() => { sys.error(""); 2 })
          val f2 = f0 or f1
          f2() must_== 1
      }
    }
  }

  "The Future[A]" can {
    "be joined with a Future[B] to create a Future[(A, B)]" in {
      withExec {
        exec =>
          val f0 = exec(() => 1)
          val f1 = exec(() => 2)
          val f2 = f0 join f1
          f2() must_==  (1, 2)
      }
    }

    "be combined with a Future[B >: A] and the first to succeed wins" in {
      withExec {
        exec =>
          val f0 = exec(() => { Thread.sleep(1000L); 1 })
          val f1 = exec(() => 2)
          val f2 = f0 or f1
          f2() must_== 2
      }

      withExec {
        exec =>
          val f0 = exec(() => 1)
          val f1 = exec(() => { Thread.sleep(1000L); 2 })
          val f2 = f0 or f1
          f2() must_== 1
      }
    }
  }
}