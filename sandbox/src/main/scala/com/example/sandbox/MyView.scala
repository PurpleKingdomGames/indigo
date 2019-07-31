package com.example.sandbox

import indigo._
import indigo.AsString._

object MyView {

  val dudeCloneId: CloneId = CloneId("Dude")

  def updateView(model: MyGameModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment = {
    frameInputEvents.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position.show)
      case None           => ()
    }

    SceneUpdateFragment(
      gameLayer(model),
      lightingLayer(frameInputEvents.signals),
      uiLayer(frameInputEvents),
      Tint.White.withAmount(0.25),
      Nil,
      SceneAudio.None,
      Nil
    ).addCloneBlanks(CloneBlank(dudeCloneId, model.dude.dude.sprite))
      // .withSaturationLevel(0.5)
      // .withTint(Tint.Cyan.withAmount(0.25))
      // .withLightingLayerColorOverlay(Tint.White.withAmount(1))
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
      currentState.dude.dude.sprite.moveBy(8, 10).withAlpha(1).withTint(Tint.Green.withAmount(0.25)),
      currentState.dude.dude.sprite.moveBy(8, -10).withAlpha(0.5).withTint(Tint.Red.withAmount(0.75)),
      Clone(dudeCloneId, Depth(1), CloneTransformData.startAt(Point(10, 50)))
    )

  def lightingLayer(signals: Signals): List[SceneGraphNode] =
    List(
      Graphic(114, 64 - 20, 320, 240, 1, MyAssets.light).withRef(Point(160, 120)).withTint(Tint.Red),
      Graphic(114 - 20, 64 + 20, 320, 240, 1, MyAssets.light).withRef(Point(160, 120)).withTint(Tint.Green),
      Graphic(114 + 20, 64 + 20, 320, 240, 1, MyAssets.light).withRef(Point(160, 120)).withTint(Tint.Blue),
      Graphic(0, 0, 320, 240, 1, MyAssets.light)
        .withTint(1, 1, 0.0, 1)
        .withAlpha(1)
        .withRef(Point(160, 120))
        .moveTo(signals.mousePosition.x, signals.mousePosition.y)
    )

  val fontKey: FontKey = FontKey("My font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, MyAssets.smallFontName, 320, 230, FontChar("a", 3, 78, 23, 23)).isCaseInSensitive
      .addChar(FontChar("A", 3, 78, 23, 23))
      .addChar(FontChar("B", 26, 78, 23, 23))
      .addChar(FontChar("C", 50, 78, 23, 23))
      .addChar(FontChar("D", 73, 78, 23, 23))
      .addChar(FontChar("E", 96, 78, 23, 23))
      .addChar(FontChar("F", 119, 78, 23, 23))
      .addChar(FontChar("G", 142, 78, 23, 23))
      .addChar(FontChar("H", 165, 78, 23, 23))
      .addChar(FontChar("I", 188, 78, 15, 23))
      .addChar(FontChar("J", 202, 78, 23, 23))
      .addChar(FontChar("K", 225, 78, 23, 23))
      .addChar(FontChar("L", 248, 78, 23, 23))
      .addChar(FontChar("M", 271, 78, 23, 23))
      .addChar(FontChar("N", 3, 104, 23, 23))
      .addChar(FontChar("O", 29, 104, 23, 23))
      .addChar(FontChar("P", 54, 104, 23, 23))
      .addChar(FontChar("Q", 75, 104, 23, 23))
      .addChar(FontChar("R", 101, 104, 23, 23))
      .addChar(FontChar("S", 124, 104, 23, 23))
      .addChar(FontChar("T", 148, 104, 23, 23))
      .addChar(FontChar("U", 173, 104, 23, 23))
      .addChar(FontChar("V", 197, 104, 23, 23))
      .addChar(FontChar("W", 220, 104, 23, 23))
      .addChar(FontChar("X", 248, 104, 23, 23))
      .addChar(FontChar("Y", 271, 104, 23, 23))
      .addChar(FontChar("Z", 297, 104, 23, 23))
      .addChar(FontChar("0", 3, 26, 23, 23))
      .addChar(FontChar("1", 26, 26, 15, 23))
      .addChar(FontChar("2", 41, 26, 23, 23))
      .addChar(FontChar("3", 64, 26, 23, 23))
      .addChar(FontChar("4", 87, 26, 23, 23))
      .addChar(FontChar("5", 110, 26, 23, 23))
      .addChar(FontChar("6", 133, 26, 23, 23))
      .addChar(FontChar("7", 156, 26, 23, 23))
      .addChar(FontChar("8", 179, 26, 23, 23))
      .addChar(FontChar("9", 202, 26, 23, 23))
      .addChar(FontChar("?", 93, 52, 23, 23))
      .addChar(FontChar("!", 3, 0, 15, 23))
      .addChar(FontChar(".", 286, 0, 15, 23))
      .addChar(FontChar(",", 248, 0, 15, 23))
      .addChar(FontChar(" ", 145, 52, 23, 23))

  def uiLayer(frameInputEvents: FrameInputEvents): List[SceneGraphNode] =
    List(
      Text("AB!\n!C", 2, 2, 5, fontKey).alignLeft,
      Text("AB!\n!C", 100, 2, 5, fontKey).alignCenter,
      Text("AB!\n!C", 200, 2, 5, fontKey).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if (frameInputEvents.wasMouseClickedWithin(bounds))
            println("Hit me! Oh yeah!")
          Nil

        case _ => Nil
      }
    )

}
