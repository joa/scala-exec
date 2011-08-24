package scala.util.concurrent.java

import java.util.concurrent.{Callable => JCallable}

/**
 * @author Joa Ebert
 */
protected final class Callable[A](val f: () => A)
    extends JCallable[A] with WithFunction[A] with (() => A) {
  override def call(): A = f()

  override def apply(): A = f()
}
