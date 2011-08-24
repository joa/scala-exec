package scala.util.concurrent.java

import scala.util.concurrent.Executor
import java.util.concurrent.{Executor => JExecutor}

/**
 * @author Joa Ebert
 */
class ExecutorWrapper(executor: JExecutor) extends Executor {
  def execute[A](f: () => A) {
    executor.execute(JavaConversions.asJavaRunnable(f))
  }
}