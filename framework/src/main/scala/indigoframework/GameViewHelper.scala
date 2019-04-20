package indigoframework

import indigo.scenegraph._
import indigo.shared.datatypes.{AmbientLight, Rectangle}
import indigo.shared.EntityRectangle

import indigo.shared.EqualTo._

final case class GameGlobalEvent()

object GameViewHelper {

  def entityRectangleToRectangle(er: EntityRectangle): Rectangle =
    Rectangle(er.x, er.y, er.width, er.height)

  def updateView(model: GameModel): SceneUpdateFragment = {
    // find active scene
    // read entities list
    // find entities
    // add to game layer
    // stand well back
    // TODO: There must always be a scene, so this optional case should never happen.
    // TODO: The types are punishing us here, something to review
    val graphics: List[Graphic] = model.gameDefinition.scenes
      .find(_.active)
      .map(_.entities)
      .getOrElse(Nil)
      .map { id =>
        model.gameDefinition.entities
          .find(_.id === id)
      }
      .collect { case Some(s) => s }
      .map {
        _.components.presentation.graphic
      }
      .flatMap {
        case Some(graphic) =>
          List(Graphic(entityRectangleToRectangle(graphic.bounds), 1, graphic.assetRef).withCrop(entityRectangleToRectangle(graphic.crop)))

        case None =>
          Nil
      }

    SceneUpdateFragment(
      graphics,
      Nil,
      Nil,
      AmbientLight.Normal,
      Nil,
      SceneAudio.None
    )
  }

}
