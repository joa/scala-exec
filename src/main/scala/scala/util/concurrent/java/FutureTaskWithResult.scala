package scala.util.concurrent.java

import java.util.concurrent.{FutureTask => JFutureTask}

/**
 * @author Joa Ebert
 */
protected final class FutureTaskWithResult[A, B](val f: () => A, result: B)
    extends JFutureTask[B](JavaConversions.asJavaRunnable(f), result) with WithFunction[A]