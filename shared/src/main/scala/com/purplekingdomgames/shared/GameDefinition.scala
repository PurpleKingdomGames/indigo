package com.purplekingdomgames.shared

import io.circe.generic.auto._
import io.circe.parser._

case class GameDefinition(scenes: List[GameScene], entities: List[Entity])
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

case class GameScene(id: String, active: Boolean, entities: List[String])

case class Entity(id: String, components: EntityComponents)
case class EntityComponents(presentation: EntityPresentation)
case class EntityPresentation(graphic: Option[EntityGraphic])
case class EntityGraphic(assetRef: String, bounds: EntityRectangle, crop: EntityRectangle)
case class EntityRectangle(x: Int, y: Int, width: Int, height: Int)
