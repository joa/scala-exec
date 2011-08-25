package scala.util.concurrent

/**
 * @author Joa Ebert
 */
object DSLSpec {
  def main(a: Array[String]) {
    import DSL._

    val factory = (x: Runnable) => {
      val result = new Thread(x)
      result.setName("test")
      result
    }

    val executor = Executors.newCachedThreadPool(factory)
    val future =
       submit { () => println(Thread.currentThread); sys.error(""+123) } to executor

    future.get(1L, TimeUnits.Seconds) match {
      case Left(err) => println("ERROR: "+err)
      case Right(value) => println(value)
    }

    executor.terminate()
  }
}