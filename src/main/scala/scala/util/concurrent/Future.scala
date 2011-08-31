package scala.util.concurrent

/**
 * @author Joa Ebert
 */
trait Future[+A] extends (() => A) {
  original =>

  /**
   * Cancels the computation of this Future.
   */
  def cancel(mayInterruptIfRunning: Boolean)

  /**
   * Awaits the result of the Future.
   */
  def get(): Either[Throwable, A]

  /**
   * Awaits the result of the Future.
   *
   * Returns a Left(TimeoutException) if the Future could not be computed in
   * the given amount of time.
   */
  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A]

  /**
   * Whether or not the Future has been canceled.
   */
  def isCancelled: Boolean

  /**
   * Whether or not the Future has been computed.
   */
  def isDone: Boolean

  /**
   * Whether or not the Future has been computed.
   * Same as isDone.
   */
  def isDefined = isDone

  /**
   * Returns the value of the Future or throws an exception that might have occurred during its computation.
   */
  override def apply(): A = get().fold(throw _, identity)

  /**
   * Returns the value of the Future or throws an exception that might have occurred during its computation.
   */
  def apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds): A =
    get(timeout, unit).fold(throw _, identity)

  /**
   * The value of the Future if already computed.
   *
   * None is also returned if computing the Future failed for whatever reason.
   */
  def value: Option[A] =
    if(isDone) {
      get().right.toOption
    } else {
      None
    }

  /**
   * A view on this Future.
   *
   * Functions like map and flatMap will be executed on demand and
   * execute in the caller's thread.
   */
  def view = new FutureView[A] {
    override def cancel(mayInterruptIfRunning: Boolean) {
      original.cancel(mayInterruptIfRunning)
    }

    override def get(): Either[Throwable, A] = original.get()

    override def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
      original.get(timeout, unit)

    override def isCancelled: Boolean = original.isCancelled

    override def isDone: Boolean = original.isDone
  }

  def join[B](that: Future[B]): Future[(A, B)] =
    new JoinFuture(this, that)

  def or[B >: A](that: Future[B]): Future[B] =
    new OrFuture[B](this, that)

  def map[AA >: A, B](f: AA => B)(implicit exec: ExecutorService): Future[B] =
    mapWithExec(exec)(f)

  def flatMap[AA >: A, B](f: AA => Future[B])(implicit exec: ExecutorService): Future[B] =
    flatMapWithExec(exec)(f)

  def foreach[AA >: A, B](f: AA => B)(implicit exec: ExecutorService) {
    foreachWithExec(exec)(f)
  }
  
  def mapWithExec[AA >: A, B](exec: ExecutorService)(f: AA => B): Future[B] =
    exec.submit(() => { f(original.apply()) })

  def flatMapWithExec[AA >: A, B](exec: ExecutorService)(f: AA => Future[B]): Future[B] =
    exec.submit(() => { f(original.apply()).apply() })

  def foreachWithExec[AA >: A, B](exec: ExecutorService)(f: AA => B) {
    exec.submit(() => f(original.apply()))
  }
}

trait FutureView[+A] extends Future[A] {
  original =>

  def map[AA >: A, B](f: AA => B): FutureView[B] =
    new FutureView[B] {
      override def cancel(mayInterruptIfRunning: Boolean) {
        original.cancel(mayInterruptIfRunning)
      }

      override def get(): Either[Throwable, B] = applyMap(original.get())

      override def get(timeout: Long, unit: TimeUnit): Either[Throwable, B] =
        applyMap(original.get(timeout, unit))

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone

      private def applyMap(value: Either[Throwable, A]): Either[Throwable, B] =
        value match {
          case Left(x) => Left[Throwable, B](x)
          case Right(x) => Right(f(x))
        }
    }

  def flatMap[AA >: A, B](f: AA => Future[B]): FutureView[B] =
    new FutureView[B] {
      override def cancel(mayInterruptIfRunning: Boolean) {
        original.cancel(mayInterruptIfRunning)
      }

      override def get(): Either[Throwable, B] = applyFlatMap(original.get()).get()

      override def get(timeout: Long, unit: TimeUnit): Either[Throwable, B] =
        applyFlatMap(original.get(timeout, unit)).get(timeout, unit)

      override def isCancelled: Boolean = original.isCancelled

      override def isDone: Boolean = original.isDone

      private def applyFlatMap(value: Either[Throwable, A]): Future[B] =
        value match {
          case Left(x) => new ErrorPreservingFuture(x)
          case Right(x) => f(x)
        }
    }

  def foreach[AA >: A, B](f: AA => B) {
    f(apply())
  }
}

private final class ErrorPreservingFuture[+A](error: Throwable) extends Future[A] {
  def cancel(mayInterruptIfRunning: Boolean) {}

  def get(): Either[Throwable, A] = Left(error)

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
    Left(error)

  def isCancelled: Boolean = false

  def isDone: Boolean = true
}

private final class JoinFuture[A, B](left: Future[A], right: Future[B]) extends Future[(A, B)] {
  def cancel(mayInterruptIfRunning: Boolean) {
    left.cancel(mayInterruptIfRunning)
    right.cancel(mayInterruptIfRunning)
  }

  def get(): Either[Throwable, (A, B)] = applyJoin(left.get(), right.get())

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, (A, B)] =
    applyJoin(left.get(timeout, unit), right.get(timeout, unit))

  def isCancelled: Boolean = left.isCancelled || right.isCancelled

  def isDone: Boolean = left.isDone && right.isDone

  private def applyJoin(left: Either[Throwable, A], right: Either[Throwable, B]): Either[Throwable, (A, B)] =
    left match {
      case Left(x) => Left[Throwable, (A, B)](x)
      case Right(l) =>
        right match {
          case Left(x) => Left[Throwable, (A, B)](x)
          case Right(r) => Right((l, r))
        }
    }
}

private final class OrFuture[A](left: Future[A], right: Future[A]) extends Future[A] {
  // TODO how should an OR future behave? e.g. should we cancel the future which did not win the race?
  
  @volatile private var cancelled = false
  @volatile private var result: Option[Either[Throwable, A]] = None

  def cancel(mayInterruptIfRunning: Boolean) {
    synchronized {
      cancelled = true
      result = Some(Left(new CancellationException()))
    }
    
    left.cancel(mayInterruptIfRunning)
    right.cancel(mayInterruptIfRunning)
  }

  def get(): Either[Throwable, A] =  get(Long.MaxValue, TimeUnits.Nanoseconds)

  def get(timeout: Long, unit: TimeUnit): Either[Throwable, A] =
    result match {
      case Some(x) => x
      case None =>
        val t0 = System.nanoTime()
        val diff = unit.toNanos(timeout)

        do {
          if(left.isDefined) {
            left.get() match {
              case Left(_) =>
                if(right.isDefined) {
                  result = Some(right.get())
                }
              case x @ Right(_) => result = Some(x)
            }
          }

          if(right.isDefined) {
            right.get() match {
              case Left(_) =>
                if(left.isDefined) {
                  result = Some(left.get())
                }
              case x @ Right(_) => result = Some(x)
            }
          }

          if((System.nanoTime() - t0) > diff) {
            result = Some(Left(new TimeoutException()))
          }

          if(!result.isDefined) {
            // Can we do this different by using a monitor and wait?
            Thread.sleep(1L)
          }
        } while(!cancelled && !result.isDefined);

        result.get
    }

  def isCancelled: Boolean = cancelled

  def isDone: Boolean = result.isDefined
}