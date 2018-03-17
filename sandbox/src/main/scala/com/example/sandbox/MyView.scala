package com.example.sandbox

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GlobalSignals, MouseEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{FontChar, FontInfo}

object MyView {

  def updateView(model: MyGameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate = {
    frameInputEvents.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position)
      case None => ()
    }
    
    SceneGraphUpdate(
      SceneGraphRootNode(
        game = gameLayer(model),
        lighting = lightingLayer,
        ui = uiLayer(frameInputEvents)
      ),
      Nil
    )
  }

  def gameLayer(currentState: MyGameModel): SceneGraphGameLayer =
    SceneGraphGameLayer(
      SceneGraphNodeBranch(
        currentState.dude.walkDirection match {
          case d@DudeLeft =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeRight =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeUp =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeDown =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()

          case d@DudeIdle =>
            currentState.dude.dude.sprite
              .changeCycle(d.cycleName)
              .play()
        },
        currentState.dude.dude.sprite.moveBy(8, 10).withAlpha(0.5).withTint(0, 1, 0)
      )
    )

  def lightingLayer: SceneGraphLightingLayer =
    SceneGraphLightingLayer(
      Graphic(0, 0, 320, 240, 1, MyAssets.light).withTint(1, 0, 0),
      Graphic(-115, -100, 320, 240, 1, MyAssets.light),
      Graphic(GlobalSignals.MousePosition.x - 160, GlobalSignals.MousePosition.y - 120, 320, 240, 1, MyAssets.light)
    ).withAmbientLightAmount(0.5).withAmbientLightTint(1, 1, 0)

  private val fontInfo: FontInfo =

    FontInfo(MyAssets.smallFontName, 320, 230, FontChar("a", 3, 78, 23, 23))
      .isCaseInSensitive
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))

  def uiLayer(frameInputEvents: FrameInputEvents): SceneGraphUiLayer =
    SceneGraphUiLayer(
      Text("AB!\n!C", 2, 2, 5, fontInfo).alignLeft,
      Text("AB!\n!C", 100, 2, 5, fontInfo).alignCenter,
      Text("AB!\n!C", 200, 2, 5, fontInfo).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if(frameInputEvents.wasMouseClickedWithin(bounds)) {
            println("Hit me! Oh yeah!")
          }
          None

        case _ => None
      }
    )

}
