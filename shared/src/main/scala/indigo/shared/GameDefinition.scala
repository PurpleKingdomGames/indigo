package indigo.shared

import io.circe.generic.auto._
import io.circe.parser._

final case class GameDefinition(scenes: List[GameScene], entities: List[Entity])
object GameDefinition {

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  def fromJson(json: String): Either[String, GameDefinition] =
    decode[GameDefinition](json) match {
      case Right(gd) =>
        Right(gd)

      case Left(e) =>
        Left("Failed to deserialise json into GameDefinition: " + e.getMessage)
    }

}

final case class GameScene(id: String, active: Boolean, entities: List[String])

final case class Entity(id: String, components: EntityComponents)
final case class EntityComponents(presentation: EntityPresentation)
final case class EntityPresentation(graphic: Option[EntityGraphic])
final case class EntityGraphic(assetRef: String, bounds: EntityRectangle, crop: EntityRectangle)
final case class EntityRectangle(x: Int, y: Int, width: Int, height: Int)
