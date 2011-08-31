# Introduction
The _scala-exec_ project aims to provide Scala wrappers for the `java.util.concurrent` package.

A main goal is to have a more convenient interface for the `ExecutorService`.

# Example
The implementation is straight forward and does not expose any Java specific classes[1].

If you know the `java.util.concurrent` package you will feel comfortable immediately.

```scala
import scala.util.concurrent._

def task() = {
  Thread.sleep(1000L) //heavy calculation
  true
}

val exec = Executors.newCachedThreadPool()
val f0 = exec(task)
val f1 = exec(task)

f0.get(500L) match { // block for 500ms
  case Right(value) => println(value) //we will probably not get here
  case Left(error) => println("ERROR: "+error) //it is more likely we reach this one
}

f1.get(2L, TimeUnits.Seconds) match {
  case Right(value) => println(value) //we should see "true"
  case Left(error) => println("ERROR: "+error) //very unlikely we reach this one
}

println(f0()) //we should see "true"

exec.terminate() //performs shutdown and awaitTermination
```

## Using the DSL
_scala-exec_ comes with a simple DSL to make the code more conciese and self explanatory.
It is up to the user whether or not the DSL should be used.

```scala
import scala.util.concurrent._
import DSL._

using(Executors.newSingleThreadExecutor()) {
  exec =>
    val future = 
      submit { 
        () =>
          Thread.sleep(1000L) //heavy calculation
          true
      } to exec
      
    println(future()) //waits for 1sec and prints Right(true)
}

val newThread =
  fork {
    Thread.sleep(1000L)
  }
```


# Implementation

The `scala.util.concurrent` package is language agnostic and not Java specific[2]. 
Java related classes can be found in `scala.util.concurrent.java`.

## ThreadPoolExecutor
The `scala.util.concurrent.java.ThreadPoolExecutor` is a wrapper for `java.util.concurrent.ThreadPoolExecutor` and
can be conviniently configured.

```scala
import scala.util.concurrent._
import java._
import _root_.java.util.concurrent.{ThreadPoolExecutor => JThreadPoolExecutor}

val exec = new ThreadPoolExecutor(
  1, 1,
  Long.MaxValue, TimeUnits.Nanoseconds,
  handler = new JThreadPoolExecutor.CallerRunsPolicy() //named arguments ftw!
)
  
exec.maximumPoolSize = 4

println(exec.taskCount)

val task =
  () => {
    Thread.sleep(1000L)
    42
  }

val futures = exec.invokeAll(task :: task :: task :: Nil)

for {
  future <- futures
} {
  println(future())
}
```
### shutdownNow vs. shutdownAndGetPending

The `shutdownNow` method has been renamed to `shutdownAndGetPending` because it allows us to return a `Seq[() => _]` while inheriting from `java.util.concurrent.AbstractExecutorService`. 

It would have been possible to have a `shutdownNow(): Seq[() => _]` method defined if the Scala version of `ThreadPoolExecutor` would implement methods like `invokeAll` etc. on its own.

Since this is error prone and we can reuse a lot of JDK classes this way this is a tradeoff but we rely on already battle tested classes.

The following code is an example of how `shutdownAndGetPending` can be used.

```scala
val exec = Executors.newCachedThreadPool()
val task = () => "Y u no execute me?"

exec.maximumPoolSize = 1

//fill the pool with some garbage
for(i <- 0 to 10) {
  exec(() => Thread.sleep(1000L))
}

exec(task)

val pending = exec.shutdownAndGetPending()
println(pending contains task) //probably true

//if you feel like it you can execute all pending tasks
pending foreach { _() }
```

## Callable and Runnable
You will never have to create an anonymous Callable or Runnable since _scala-exec_ performs this conversion for you.

The library works always with `() => A` for cases where the Java version would use a `Runnable` or `Callable<V>`.

## Future
The `scala.util.concurrent.Future[A]` mirrors `java.util.concurrent.Future<V>` with small modifications.

* `get()` and `get(timeout: Long, unit: TimeUnit)` return `Either[Throwable, A]`.
* `scala.util.concurrent.Future[A]` extends `() => A`.
* The additional `apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds)` method.
* `or[B >: A](that: Future[B]): Future[B]` to select the first Future which is done.
* `join[B](that: Future[B]): Future[(A, B)]` to combine two Futures.
* `value: Option[A]` which returns `Some` value if computed.
* The `view` method to create a `FutureView[A]`.
* `map`, `flatMap` and `foreach`
* `mapWithExec`, `flatMapWithExec` and `forEachWithExec`

This mans you are able to use the following shortcuts:

```scala
implicit val exec = Executors.newCachedThreadPool()
val exec2 = Executors.newCachedThreadPool()

def task() = "hello world"

val f = exec(task)

f() //block until done
f(100L) //block for 100ms
f(100L, TimeUnits.Nanoseconds) //block for 100ns
f.get(1L).right getOrElse "default"

val f0 = exec(task) map { _.toUpperCase } // _.toUpperCase will be executed using exec via implicit
val f1 = exec(task) mapWithExec(exec2) { _.toUpperCase } //explicitly choose exec2

println(f0())
println(f1())


val f2 = f.view map { _.toUpperCase }
f2() //the map function will be executed on demand in callers thread
```

---

[1] The current `ThreadPoolExecutor` implementation does leak `RejectedExecutionHandler` and `BlockingQueue` types which is subject to change.

[2] For simplicity reasons the `Executors` object is an exception.
