package apigen

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
import io.circe.syntax._

sealed trait EntityDefinition {
  val name: String
  val entityType: String

  def addMembers(members: List[EntityDefinition]): EntityDefinition =
    this match {
      case e: ClassEntity =>
        e.copy(members = members)

      case e: StaticEntity =>
        e.copy(members = members)

      case e =>
        e
    }
}

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class ClassEntity(name: String, members: List[EntityDefinition], entityType: String = "class") extends EntityDefinition
@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class StaticEntity(name: String, members: List[EntityDefinition], entityType: String = "static") extends EntityDefinition
@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class ValueEntity(name: String, returnType: String, entityType: String = "value") extends EntityDefinition
@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class MethodEntity(name: String, entityType: String = "method") extends EntityDefinition
@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
final case class FunctionEntity(name: String, entityType: String = "function") extends EntityDefinition

@SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
object EntityDefinition {
  implicit val encodeEvent: Encoder[EntityDefinition] = Encoder.instance {
    case x: ClassEntity    => x.asJson
    case x: StaticEntity   => x.asJson
    case x: ValueEntity    => x.asJson
    case x: MethodEntity   => x.asJson
    case x: FunctionEntity => x.asJson
  }

  implicit val decodeEvent: Decoder[EntityDefinition] =
    List[Decoder[EntityDefinition]](
      Decoder[ClassEntity].widen,
      Decoder[StaticEntity].widen,
      Decoder[ValueEntity].widen,
      Decoder[MethodEntity].widen,
      Decoder[FunctionEntity].widen
    ).reduceLeft(_ or _)
}
