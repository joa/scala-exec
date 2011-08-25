package scala.util.concurrent

/**
 * @author Joa Ebert
 */
object DSL {
  final class To[A](f: () => A) {
    def to(executor: ExecutorService): Future[A] = executor.submit(f)
  }
  
  def submit[A](f: () => A): To[A] = new To(f)

  def using[A <: ExecutorService, B](executor: A)(f: A => B) =
    try {
      f(executor)
    } finally {
      executor.terminate()
    }
}