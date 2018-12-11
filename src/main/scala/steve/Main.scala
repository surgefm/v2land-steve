package steve

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.io.StdIn
import scala.sys.process.Process
import java.io.File

import steve.slack.CommandResponse

object WebServer extends steve.slack.JsonSupport {

  implicit val system = ActorSystem("my-system")
  lazy val gitService = system.actorOf(Props[GitService], "git-service")

  def main(args: Array[String]) {
    val conf = ConfigFactory.load()
    val settings = new Settings(conf)

    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val route =
      pathPrefix("command") {
        pathSingleSlash {
          get {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
        } ~
        path("list") {
          post {
            val futures = settings.entries.map { entry =>
              Future {
                val branch = Process("git symbolic-ref --short HEAD", new File(entry.workDir)).!!
                val hash = Process("git rev-parse HEAD", new File(entry.workDir)).!!
                slack.Message(
                  s"Name: ${entry.name}\n" +
                  s"Branch: ${branch}" +
                  s"Hash: ${hash}"
                )
              }
            }

            val resp = Future.sequence(futures).map { attachments =>
              CommandResponse(
                "in_channel",
                "List of all repos:",
                attachments
              )
            }

            onSuccess(resp) { complete(_) }
          }
        } ~
        path("update") {
          post {
            formFields('text, 'user_name) { (serviceName, userName) =>
              complete(CommandResponse(
                "in_channel",
                s"Staring a process by ${userName} for updating repo: ${serviceName}..."
              ))
            }
          }
        } ~
        path("switch") {
          post {
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
