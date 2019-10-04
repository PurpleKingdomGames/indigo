import cats.syntax.functor._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.parser.decode

sealed trait ElmMessage
final case class DoubleIt(amount: Int) extends ElmMessage

object ElmMessage {

  implicit val encodeEvent: Encoder[ElmMessage] = Encoder.instance {
    case doubleIt @ DoubleIt(_) => doubleIt.asJson
  }

  implicit val decodeEvent: Decoder[ElmMessage] =
    List[Decoder[ElmMessage]](
      Decoder[DoubleIt].widen
    ).reduceLeft(_ or _)

}
