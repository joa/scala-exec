package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Executor {
	def execute(f: => Unit)
}
