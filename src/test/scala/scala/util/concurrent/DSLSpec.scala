package scala.util.concurrent

import org.specs2.mutable._

/**
 * @author Joa Ebert
 */
class DSLSpec extends Specification {
  "The scala-exec DSL" should {
    "offer a using-directive which terminates an executor" in {
      "even when an exception is trown" in {
        import DSL._

        val executor = Executors.newSingleThreadExecutor()

        using(executor) {
          _ match {
            case x if true == false => "Not reachable by definition."
            case _ => throw new Exception("Expected exception.")
          }
        } must throwA[Exception]

        executor.isShutdown must beTrue
        executor.isTerminated must beTrue
      }

      "when everything works as expected" in {
        import DSL._

        val executor = Executors.newSingleThreadExecutor()

        using(executor) { e => true } must beTrue

        executor.isShutdown must beTrue
        executor.isTerminated must beTrue
      }
    }

    "support 'submit { () => A } to ExecutorService' syntax and return a Future[A]" in {
      import DSL._

      using(Executors.newSingleThreadExecutor()) {
        executor =>
          val f = submit { () => true } to executor
          f(100L) must beTrue
      }
    }
  }
}