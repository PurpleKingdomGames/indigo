package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.util.Logger
import upickle.default._

case class GameDefinition(scenes: List[GameScene])
case class GameScene(id: String, active: Boolean, scenegraph: List[GameNode])
case class GameNode(nodeType: String, assetRef: String, bounds: GameRectangle)
case class GameRectangle(x: Int, y: Int, width: Int, height: Int)

object GameDefinitionHelper {

  def fromJson(json: String): Option[GameDefinition] =
    try {
      Option(read[GameDefinition](json))
    } catch {
      case e: Throwable =>
        Logger.info("Failed to deserialise json into GameDefinition: " + e.getMessage)
        None
    }

}
