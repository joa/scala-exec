import compat.Platform
import java.net.ServerSocket
import java.nio.Buffer
import java.sql.ClientInfoStatus
import util.concurrent.Executors

/**
 * Compile with -P:continuations:enable
 * 
 * @author Joa Ebert
 */
object AsyncServerExample {
  import scala.util.concurrent.fork
  import scala.util.concurrent.DSL._

  def main(args: Array[String]) {
    val server = new Server()
    val clients = List.fill(16) { new Client("happyhappyboingboingyipyip") }
    val exec = Executors.newFixedThreadPool(2)

    fork {
      server.run()
    }

    val futures =
      for {
        client <- clients
      } yield {
        submit { client.run _ } to exec
      }

    futures foreach { _() }
    exec.terminate()
    System.exit(0)
  }

  def quietly[U](f: => U) = try { f } catch { case _ => }

  private final class Client(message: String) {
    def run() {
      import _root_.java.net.{Socket => JSocket, InetAddress => JInetAddress}
      import _root_.java.io.{InputStreamReader => JInputStreamReader}
      import _root_.java.io.{BufferedReader => JBufferedReader}

      val socket = new JSocket(JInetAddress.getLocalHost, 9999)
      val input =  new JBufferedReader(new JInputStreamReader(socket.getInputStream))
      val output = socket.getOutputStream

      try {
        println("[CLIENT] Sending \""+message+"\" ...")
        output.write((message+"\n").getBytes("utf-8"))
        output.flush()
        println("[CLIENT] Received \""+input.readLine()+"\".")
      } finally {
        quietly { output.close() }
        quietly { input.close() }
        quietly { socket.close() }
      }
    }
  }

  private final class Server {
    import scala.util.concurrent._
    import scala.util.concurrent.DSL._
    import scala.util.continuations._
    import _root_.java.net.{ServerSocket => JServerSocket}
    import _root_.java.io.{InputStreamReader => JInputStreamReader}
    import _root_.java.io.{BufferedReader => JBufferedReader}

    private val exec =
      Executors.newFixedThreadPool(4)

    def run() {
      while(true) {
        try {
          reset {
            val server = new JServerSocket(9999, 1)
            val socket = server.accept()
            server.close()

            continue()

            // Of course you can improve this since here are again a lot of
            // blocking calls like readLine or Thread.sleep.

            val input =
              new JBufferedReader(new JInputStreamReader(socket.getInputStream))
            val output = socket.getOutputStream

            input.readLine() match {
              case other =>
                val reply = other.toUpperCase
                Thread.sleep(250L)
                println("[SERVER] Sending \""+reply+"\" back to client.")
                output.write((reply+"\n").getBytes("utf-8"))
            }

            output.flush()

            quietly { output.close() }
            quietly { input.close() }
            quietly { socket.close() }
          }
        } catch {
          case _ =>
        }
      }
    }

    def continue() =
      shift {
        k: (Unit => _) => {
          submit {
            () => k()
          } to exec
        }
      }
  }
}