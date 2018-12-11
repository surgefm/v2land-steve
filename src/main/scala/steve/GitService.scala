package steve

import akka.actor.Actor
import steve.GitService.UpdateService

object GitService {
  sealed trait Message
  case class UpdateService(entry: Entry)
}

class GitService extends Actor {

  override def receive: Receive = {
    case UpdateService(config) =>
      print("hello")
  }

}
