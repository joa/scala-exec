package scala.util.concurrent.java

import java.util.concurrent.{FutureTask => JFutureTask}

/**
 * @author Joa Ebert
 */
protected final class FutureTask[A](val f: () => A)
    extends JFutureTask[A](JavaConversions.asJavaCallable(f)) with WithFunction[A]