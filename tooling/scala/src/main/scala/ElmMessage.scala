import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

sealed trait ElmMessage
final case class LogIt(message: String) extends ElmMessage
final case class DoubleIt(amount: Int) extends ElmMessage

object ElmMessage {

  implicit val encodeEvent: Encoder[ElmMessage] = Encoder.instance {
    case s @ LogIt(_) => s.asJson
    case d @ DoubleIt(_)       => d.asJson
  }

  implicit val decodeEvent: Decoder[ElmMessage] =
    List[Decoder[ElmMessage]](
      Decoder[LogIt].widen,
      Decoder[DoubleIt].widen
    ).reduceLeft(_ or _)

}
