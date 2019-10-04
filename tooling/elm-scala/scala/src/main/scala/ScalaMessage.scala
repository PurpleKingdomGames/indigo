import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

sealed trait ScalaMessage
final case class Doubled(amount: Int) extends ScalaMessage

object ScalaMessage {

  implicit val encodeEvent: Encoder[ScalaMessage] = Encoder.instance {
    case doubleIt @ Doubled(_) => doubleIt.asJson
  }

  implicit val decodeEvent: Decoder[ScalaMessage] =
    List[Decoder[ScalaMessage]](
      Decoder[Doubled].widen
    ).reduceLeft(_ or _)

}
