package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Rectangle
import com.purplekingdomgames.shared.EntityRectangle

case class GameViewEvent()

object GameViewHelper {

  implicit def entityRectangleToRectangle(er: EntityRectangle): Rectangle =
    Rectangle(er.x, er.y, er.width, er.height)

  def updateView(model: GameModel): SceneGraphUpdate[GameViewEvent] = {
    // find active scene
    // read entities list
    // find entities
    // add to game layer
    // stand well back
    // TODO: There must always be a scene, so this optional case should never happen.
    // TODO: The types are punishing us here, something to review
    val graphics: List[Graphic[GameViewEvent]] = model
      .gameDefinition
      .scenes
      .find(_.active)
      .map(_.entities)
      .getOrElse(Nil)
      .map { id =>
        model
          .gameDefinition
          .entities
          .find(_.id == id)
      }
      .collect { case Some(s) => s }
      .flatMap(_.components.presentation.graphic)
      .map { graphic =>
        Graphic[GameViewEvent](graphic.bounds, 1, graphic.assetRef).withCrop(graphic.crop)
      }

    SceneGraphUpdate(
      SceneGraphRootNode(
        game = SceneGraphGameLayer[GameViewEvent](graphics),
        lighting = SceneGraphLightingLayer.empty,
        ui = SceneGraphUiLayer.empty
      ),
      Nil
    )
  }

}
