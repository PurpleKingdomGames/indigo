import scala.scalajs.js.annotation._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

trait ScalaToElmBridge {

  def process: ElmMessage => ScalaMessage

  @JSExport
  def post(msg: String): String =
    decode[ElmMessage](msg) match {
      case Left(e) =>
        s"""{"error" : "${e.getMessage()}"}"""

      case Right(elmMessage) =>
        process(elmMessage).asJson.noSpaces

    }

}
