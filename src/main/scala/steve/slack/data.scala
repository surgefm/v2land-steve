package steve.slack

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

final case class Error(
                      response_type: String,
                      text: String,
                      )

final case class Message(
                        text: String,
                        )

final case class CommandResponse(
                                response_type: String,
                                text: String,
                                attachments: List[Message] = List(),
                                )

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val errorFormat = jsonFormat2(Error)
  implicit val messageFormat = jsonFormat1(Message)
  implicit val commandRespFormat = jsonFormat3(CommandResponse)
}
