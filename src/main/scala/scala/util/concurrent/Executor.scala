package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Executor {
	def execute[A](f: () => A)
}
