package scala.util.concurrent

/**
 * @author Joa Ebert
 */
sealed trait TimeUnit {
	def toNanos(x: Long): Long
}

case object Nanoseconds extends TimeUnit {
  override def toNanos(x: Long) = x
}

case object Microseconds extends TimeUnit {
  override def toNanos(x: Long) = x * 1000L
}
case object Milliseconds extends TimeUnit {
	override def toNanos(x: Long) = x * 1000L * 1000L
}

case object Seconds extends TimeUnit {
	private val UnitConversionFactor = 1000L * 1000L * 1000L
	
	override def toNanos(x: Long) = x * UnitConversionFactor
}

case object Minutes extends TimeUnit {
	private val UnitConversionFactor = 60L * 1000L * 1000L * 1000L

	override def toNanos(x: Long) = x * UnitConversionFactor
}

case object Hours extends TimeUnit {
	private val UnitConversionFactor = 60L * 60L * 1000L * 1000L * 1000L

	override def toNanos(x: Long) = x * UnitConversionFactor
}

case object Days extends TimeUnit {
	private val UnitConversionFactor = 24L * 60L * 60L * 1000L * 1000L * 1000L

	override def toNanos(x: Long) = x * UnitConversionFactor
}