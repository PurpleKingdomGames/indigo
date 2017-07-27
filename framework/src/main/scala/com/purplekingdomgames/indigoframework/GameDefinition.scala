package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.util.Logger
import io.circe.generic.auto._
import io.circe.parser._

case class GameDefinition(scenes: List[GameScene], entities: List[Entity])
case class GameScene(id: String, active: Boolean, scenegraph: List[GameNode])
case class GameNode(entityId: Option[String], children: Option[List[GameNode]])

object GameDefinitionHelper {

  def fromJson(json: String): Option[GameDefinition] =
    decode[GameDefinition](json) match {
      case Right(gd) => Some(gd)
      case Left(e) =>
        Logger.info("Failed to deserialise json into GameDefinition: " + e.getMessage)
        None
    }

}

case class Entity(id: String, components: EntityComponents)
case class EntityComponents(presentation: EntityPresentation)
case class EntityPresentation(graphic: Option[EntityGraphic])
case class EntityGraphic(assetRef: String, bounds: EntityRectangle, crop: EntityRectangle)
case class EntityRectangle(x: Int, y: Int, width: Int, height: Int)
