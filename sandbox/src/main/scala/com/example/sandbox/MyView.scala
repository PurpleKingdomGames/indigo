package com.example.sandbox

import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GlobalSignals, MouseEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes._
import com.purplekingdomgames.indigo.runtime.Show

object MyView {

  def updateView(model: MyGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    frameInputEvents.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + implicitly[Show[Point]].show(position))
      case None           => ()
    }

    SceneUpdateFragment(
      gameLayer(model),
      lightingLayer,
      uiLayer(frameInputEvents),
      AmbientLight.Normal.withAmount(0.5).withTint(1, 1, 0),
      Nil,
      SceneAudio.None
    )
  }

  def gameLayer(currentState: MyGameModel): List[SceneGraphNode] =
    List(
      currentState.dude.walkDirection match {
        case d @ DudeLeft =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeRight =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeUp =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeDown =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()

        case d @ DudeIdle =>
          currentState.dude.dude.sprite
            .changeCycle(d.cycleName)
            .play()
      },
      currentState.dude.dude.sprite.moveBy(8, 10).withAlpha(0.5).withTint(0, 1, 0)
    )

  def lightingLayer: List[SceneGraphNode] =
    List(
      Graphic(0, 0, 320, 240, 1, MyAssets.light).withTint(1, 0, 0),
      Graphic(-115, -100, 320, 240, 1, MyAssets.light),
      Graphic(GlobalSignals.MousePosition.x - 160, GlobalSignals.MousePosition.y - 120, 320, 240, 1, MyAssets.light)
    )

  val fontKey: FontKey = FontKey("My font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, MyAssets.smallFontName, 320, 230, FontChar("a", 3, 78, 23, 23)).isCaseInSensitive
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))

  def uiLayer(frameInputEvents: FrameInputEvents): List[SceneGraphNode] =
    List(
      Text("AB!\n!C", 2, 2, 5, fontKey).alignLeft,
      Text("AB!\n!C", 100, 2, 5, fontKey).alignCenter,
      Text("AB!\n!C", 200, 2, 5, fontKey).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if (frameInputEvents.wasMouseClickedWithin(bounds))
            println("Hit me! Oh yeah!")
          None

        case _ => None
      }
    )

}
