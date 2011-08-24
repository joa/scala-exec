package scala.util.concurrent.java

import java.lang.{Runnable => JRunnable}

/**
 * @author Joa Ebert
 */
protected final class Runnable[A](val f: () => A)
    extends JRunnable with WithFunction[A] with (() => A) {
  override def run() {
    f()
  }

  override def apply(): A = f()
}
