package scala.util.concurrent

/**
 * @author Joa Ebert
 */
sealed trait TimeUnit {
	def toMillis(x: Long): Long
}

case object Milliseconds extends TimeUnit {
	override def toMillis(x: Long) = x
}

case object Seconds extends TimeUnit {
	private val FACTOR = 1000L
	
	override def toMillis(x: Long) = x * FACTOR
}

case object Minutes extends TimeUnit {
	private val FACTOR = 60L * 1000L

	override def toMillis(x: Long) = x * FACTOR
}

case object Hours extends TimeUnit {
	private val FACTOR = 60L * 60L * 1000L

	override def toMillis(x: Long) = x * FACTOR
}

case object Days extends TimeUnit {
	private val FACTOR = 24L * 60L * 60L * 1000L

	override def toMillis(x: Long) = x * FACTOR
}