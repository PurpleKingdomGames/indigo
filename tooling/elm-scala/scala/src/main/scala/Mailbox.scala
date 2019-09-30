import scala.scalajs.js.annotation._

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

@JSExportTopLevel("Mailbox")
object Mailbox {

  @JSExport
  def double(i: Int): Int =
    i * 2

  @JSExport
  def post(msg: String): String =
    decode[ElmMessage](msg) match {
      case Left(e) =>
        println("Problem with elm message: " + msg)
        println("Error: " + e.getMessage())
        s"""{"error" : "${e.getMessage()}"}"""

      case Right(ElmMessage.DoubleIt(i)) =>
        ScalaMessage.Doubled(double(i)).asJson.noSpaces
    }

}

sealed trait ScalaMessage

object ScalaMessage {

  final case class Doubled(amount: Int) extends ScalaMessage

  implicit val encodeEvent: Encoder[ScalaMessage] = Encoder.instance {
    case doubleIt @ Doubled(_) => doubleIt.asJson
  }

  implicit val decodeEvent: Decoder[ScalaMessage] =
    List[Decoder[ScalaMessage]](
      Decoder[Doubled].widen
    ).reduceLeft(_ or _)

}

sealed trait ElmMessage

object ElmMessage {

  final case class DoubleIt(amount: Int) extends ElmMessage

  implicit val encodeEvent: Encoder[ElmMessage] = Encoder.instance {
    case doubleIt @ DoubleIt(_) => doubleIt.asJson
  }

  implicit val decodeEvent: Decoder[ElmMessage] =
    List[Decoder[ElmMessage]](
      Decoder[DoubleIt].widen
    ).reduceLeft(_ or _)

}
