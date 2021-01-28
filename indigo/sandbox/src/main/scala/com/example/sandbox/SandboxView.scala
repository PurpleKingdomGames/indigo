package com.example.sandbox

import indigo._
import scala.annotation.nowarn

object SandboxView {

  val dudeCloneId: CloneId = CloneId("Dude")

  def updateView(model: SandboxGameModel, viewModel: SandboxViewModel, inputState: InputState): SceneUpdateFragment = {
    inputState.mouse.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position.toString())
      case None           => ()
    }

    SceneUpdateFragment.empty
      .addLayer(Layer(gameLayer(model, viewModel), Some(3)))
      .addLayer(Layer(gameLayer(model, viewModel).map {
        case s: Sprite => s.moveBy(10, 10)
        case n => n
      }))
    // .addGameLayerNodes(gameLayer(model, viewModel))
    // .addLightingLayerNodes(lightingLayer(inputState))
    // .addUiLayerNodes(uiLayer(inputState))
    // .withAmbientLight(RGBA.White.withAmount(0.25))
    // .addCloneBlanks(CloneBlank(dudeCloneId, model.dude.dude.sprite))
    // .withSaturationLevel(0.5)
    // .withTint(RGBA.Cyan.withAmount(0.25))
    // .withUiColorOverlay(RGBA.Black.withAmount(0.5))
    // .withGameColorOverlay(RGBA.Red.withAmount(0.5))
  }

  @nowarn
  def gameLayer(currentState: SandboxGameModel, viewModel: SandboxViewModel): List[SceneGraphNode] =
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
      }//,
      // currentState.dude.dude.sprite.moveBy(8, 10).moveBy(viewModel.offset).withAlpha(1).withTint(RGBA.Green.withAmount(0.25)),
      // currentState.dude.dude.sprite.moveBy(8, -10).withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75)),
      // Clone(dudeCloneId, Depth(1), CloneTransformData.startAt(Point(16, 64)))
      //   .withHorizontalFlip(true)
      //   .withAlpha(0.5f)
    )

  def lightingLayer(inputState: InputState): List[SceneGraphNode] =
    List(
      Graphic(114, 64 - 20, 320, 240, 1, SandboxAssets.lightMaterial).withRef(Point(160, 120)).withTint(RGBA.Red),
      Graphic(114 - 20, 64 + 20, 320, 240, 1, SandboxAssets.lightMaterial).withRef(Point(160, 120)).withTint(RGBA.Green),
      Graphic(114 + 20, 64 + 20, 320, 240, 1, SandboxAssets.lightMaterial).withRef(Point(160, 120)).withTint(RGBA.Blue),
      Graphic(0, 0, 320, 240, 1, SandboxAssets.lightMaterial)
        .withTint(1, 1, 0.0, 1)
        .withAlpha(1)
        .withRef(Point(160, 120))
        .moveTo(inputState.mouse.position.x, inputState.mouse.position.y)
    )

  val fontKey: FontKey = FontKey("Sandbox font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, SandboxAssets.smallFontNameMaterial, 320, 230, FontChar(" ", 145, 52, 23, 23)).isCaseInSensitive
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

  def uiLayer(inputState: InputState): List[SceneGraphNode] =
    List(
      Text("AB!\n!C", 2, 2, 5, fontKey).alignLeft,
      Text("AB!\n!C", 100, 2, 5, fontKey).alignCenter,
      Text("AB!\n!C", 200, 2, 5, fontKey).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if (inputState.mouse.wasMouseClickedWithin(bounds))
            println("Hit me!")
          Nil

        case _ => Nil
      }
    )

}
