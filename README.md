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

f0(500L) match { // block for 500ms
  case Right(value) => println(value) //we will probably not get here
  case Left(error) => println("ERROR: "+error) //it is more likely we reach this one
}

f1(2L, TimeUnits.Seconds) match {
  case Right(value) => println(value) //we should see "true"
  case Left(error) => println("ERROR: "+error) //very unlikely we reach this one
}

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
  result <- future
} {
  println(result)
}

exec(task)

val pending = exec.shutdownNow()
println(pending contains task) //true

exec.terminate()
```

## Callable and Runnable
You will never have to create an anonymous Callable or Runnable since _scala-exec_ performs this conversion for you.

The library works always with `() => A` for cases where the Java version would use a `Runnable` or `Callable<V>`.

## Future
The `scala.util.concurrent.Future[A]` mirrors `java.util.concurrent.Future<V>` with small modifications.

* `get()` and `get(timeout: Long, unit: TimeUnit)` return `Either[Throwable, A]`
* `scala.util.concurrent.Future[A]` extends `() => Either[Throwable, A]`
* The additional `apply(timeout: Long, unit: TimeUnit = TimeUnits.Milliseconds)` method.

This mans you are able to use the following shortcuts:

```scala
def task() = "hello world"

val f = exec(task)

f() //block until done
f(100L) //block for 100ms
f(100L, TimeUnits.Nanoseconds) //block for 100ns
f(1L).right getOrElse "default"
```

---

[1] The current `ThreadPoolExecutor` implementation does leak `RejectedExecutionHandler` and BlockingQueue types which is subject to change.

[2] For simplicity reasons the Executors object is an exception.
