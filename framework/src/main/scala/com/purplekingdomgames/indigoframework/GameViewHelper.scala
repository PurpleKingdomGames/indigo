package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.{FrameInputEvents, GameTime}

case class GameViewEvent()

object GameViewHelper {

  def updateView(gameTime: GameTime, model: GameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[GameViewEvent] = {
//    frameInputEvents.mouseClickAt match {
//      case Some(position) => println("Mouse clicked at: " + position)
//      case None => ()
//    }
//

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
        Graphic[GameViewEvent](graphic.bounds.toRectangle, 1, graphic.assetRef).withCrop(graphic.crop.toRectangle)
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

//  def gameLayer(currentState: MyGameModel): SceneGraphGameLayer[MyViewEventDataType] =
//    SceneGraphGameLayer(
//      SceneGraphNodeBranch(
//        currentState.dude.walkDirection match {
//          case d@DudeLeft =>
//            currentState.dude.dude.sprite
//              .changeCycle(d.cycleName)
//              .play()
//
//          case d@DudeRight =>
//            currentState.dude.dude.sprite
//              .changeCycle(d.cycleName)
//              .play()
//
//          case d@DudeUp =>
//            currentState.dude.dude.sprite
//              .changeCycle(d.cycleName)
//              .play()
//
//          case d@DudeDown =>
//            currentState.dude.dude.sprite
//              .changeCycle(d.cycleName)
//              .play()
//
//          case d@DudeIdle =>
//            currentState.dude.dude.sprite
//              .changeCycle(d.cycleName)
//              .play()
//        },
//        currentState.dude.dude.sprite.moveBy(8, 10).withAlpha(0.5).withTint(0, 1, 0)
//      )
//    )
//
//  def lightingLayer(currentState: MyGameModel): SceneGraphLightingLayer[MyViewEventDataType] =
//    SceneGraphLightingLayer[MyViewEventDataType](
//      Graphic[MyViewEventDataType](0, 0, 320, 240, 1, "light").withTint(1, 0, 0),
//      Graphic[MyViewEventDataType](-115, -100, 320, 240, 1, "light"),
//      Graphic[MyViewEventDataType](GlobalSignals.MousePosition.x - 160, GlobalSignals.MousePosition.y - 120, 320, 240, 1, "light")
//    ).withAmbientLightAmount(0.5).withAmbientLightTint(1, 1, 0)
//
//  private val fontInfo: FontInfo =
//
//    FontInfo("smallFontName", 320, 230, FontChar("a", 3, 78, 23, 23))
//      .isCaseInSensitive
//      .addChar(FontChar("B", 26, 78, 23, 23))
//      .addChar(FontChar("C", 50, 78, 23, 23))
//      .addChar(FontChar("!", 3, 0, 15, 23))
//
//  def uiLayer(frameInputEvents: FrameInputEvents, currentState: MyGameModel): SceneGraphUiLayer[MyViewEventDataType] =
//    SceneGraphUiLayer(
//      Text("AB!\n!C", 2, 2, 5, fontInfo).alignLeft,
//      Text("AB!\n!C", 100, 2, 5, fontInfo).alignCenter,
//      Text("AB!\n!C", 200, 2, 5, fontInfo).alignRight.onEvent {
//        case (bounds, MouseClick(_, _)) =>
//          if(frameInputEvents.wasMouseClickedWithin(bounds)) {
//            println("Hit me! Oh yeah!")
//          }
//          None
//
//        case _ => None
//      }
//    )

}
