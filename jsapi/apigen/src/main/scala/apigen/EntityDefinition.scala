package apigen

import cats.syntax.functor._
import io.circe.{Decoder, Encoder}, io.circe.generic.auto._
import io.circe.syntax._

sealed trait EntityDefinition {
  // def toXml: String
}

final case class ClassEntity(name: String) extends EntityDefinition {
  // def toXml: String =
  //   s"""<entity type="class" name="$name"></entity>\n"""
}

final case class StaticEntity(name: String) extends EntityDefinition {
  // def toXml: String =
  //   s"""<entity type="static" name="$name"></entity>\n"""
}

final case class ValueEntity(name: String, returnType: String) extends EntityDefinition {
  // def toXml: String =
  //   s"""<entity type="value" name="$name" returnType="$returnType"></entity>\n"""
}

final case class MethodEntity(name: String) extends EntityDefinition {
  // def toXml: String =
  //   s"""<entity type="method" name="$name"></entity>\n"""
}

final case class FunctionEntity(name: String) extends EntityDefinition {
  // def toXml: String =
  //   s"""<entity type="function" name="$name"></entity>\n"""
}

@SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
object EntityDefinition {
  implicit val encodeEvent: Encoder[EntityDefinition] = Encoder.instance {
    case x @ ClassEntity(_)    => x.asJson
    case x @ StaticEntity(_)   => x.asJson
    case x @ ValueEntity(_, _) => x.asJson
    case x @ MethodEntity(_)   => x.asJson
    case x @ FunctionEntity(_) => x.asJson
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
