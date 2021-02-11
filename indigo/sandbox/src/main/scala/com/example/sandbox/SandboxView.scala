package com.example.sandbox

import indigo._

object SandboxView {

  val dudeCloneId: CloneId = CloneId("Dude")

  def updateView(model: SandboxGameModel, viewModel: SandboxViewModel, inputState: InputState): SceneUpdateFragment = {
    inputState.mouse.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position.toString())
      case None           => ()
    }

    SceneUpdateFragment.empty
      .addLayer(
        Layer(gameLayer(model, viewModel))
          .withMagnification(2)
          .withDepth(Depth(300))
          // .withBlend(Blend.Alpha)
      )
      // .addLayer(
      //   Layer(lightingLayer(inputState))
      //     .withDepth(Depth(301))
      //     .withBlend(Blend.Alpha)
      // )
      // .addLayer(Layer(uiLayer(inputState)))
      .withAmbientLight(RGBA.White.withAmount(0.25))
      .addCloneBlanks(CloneBlank(dudeCloneId, model.dude.dude.sprite))
    // .withSaturationLevel(0.5)
    // .withTint(RGBA.Cyan.withAmount(0.25))
    // .withUiColorOverlay(RGBA.Black.withAmount(0.5))
    // .withGameColorOverlay(RGBA.Red.withAmount(0.5))
  }

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
      },
      currentState.dude.dude.sprite
        .moveBy(8, 10)
        .moveBy(viewModel.offset)
        .modifyMaterial {
          case m: StandardMaterial.Blit         => m
          case m: StandardMaterial.ImageEffects => m.withAlpha(1).withTint(RGBA.Green.withAmount(0.25))
        },
      currentState.dude.dude.sprite
        .moveBy(8, -10)
        .modifyMaterial {
          case m: StandardMaterial.Blit         => m
          case m: StandardMaterial.ImageEffects => m.withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75))
        },
      Clone(dudeCloneId, Depth(1), CloneTransformData.startAt(Point(16, 64)))
        .withHorizontalFlip(true)
    )

  def lightingLayer(inputState: InputState): List[SceneGraphNode] =
    List(
      Graphic(114, 64 - 20, 320, 240, 1, SandboxAssets.lightMaterial.withTint(RGBA.Red))
        .withRef(Point(160, 120)),
      Graphic(114 - 20, 64 + 20, 320, 240, 1, SandboxAssets.lightMaterial.withTint(RGBA.Green))
        .withRef(Point(160, 120)),
      Graphic(114 + 20, 64 + 20, 320, 240, 1, SandboxAssets.lightMaterial.withTint(RGBA.Blue))
        .withRef(Point(160, 120)),
      Graphic(0, 0, 320, 240, 1, SandboxAssets.lightMaterial.withTint(RGBA(1, 1, 0.0, 1)).withAlpha(1))
        .withRef(Point(160, 120))
        .moveTo(inputState.mouse.position.x, inputState.mouse.position.y)
    )

  def uiLayer(inputState: InputState): List[SceneGraphNode] =
    List(
      Text("AB!\n!C", 2, 2, 5, Fonts.fontKey, SandboxAssets.fontMaterial).alignLeft,
      Text("AB!\n!C", 100, 2, 5, Fonts.fontKey, SandboxAssets.fontMaterial).alignCenter,
      Text("AB!\n!C", 200, 2, 5, Fonts.fontKey, SandboxAssets.fontMaterial).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if (inputState.mouse.wasMouseClickedWithin(bounds))
            println("Hit me!")
          Nil

        case _ => Nil
      }
    )

}
