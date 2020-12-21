package com.example.sandbox

import indigo._

object SandboxView {

  val dudeCloneId: CloneId = CloneId("Dude")

  // @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def updateView(running: Seconds, model: SandboxGameModel, viewModel: SandboxViewModel, inputState: InputState): SceneUpdateFragment = {
    inputState.mouse.mouseClickAt match {
      case Some(position) => println("Mouse clicked at: " + position.toString())
      case None           => ()
    }

    SceneUpdateFragment.empty
      .addGameLayerNodes(gameLayer(running, model, viewModel))
      .addLightingLayerNodes(lightingLayer(inputState))
      // .addUiLayerNodes(uiLayer(inputState))
      .withAmbientLight(RGBA.White.withAmount(0.25))
      .addCloneBlanks(CloneBlank(dudeCloneId, model.dude.dude.sprite))
      // .withSaturationLevel(0.5)
      // .withTint(RGBA.Cyan.withAmount(0.25))
      // .withUiColorOverlay(RGBA.Black.withAmount(0.5))
      // .withGameColorOverlay(RGBA.Red.withAmount(0.5))
      .withLights(
        DirectionLight(30, RGB.Green, 1.2, Radians.fromDegrees(30))//,
        // PointLight.default
        //   .moveTo(Point(250, 30))
        //   .withAttenuation(150)
        //   .withColor(RGB.Red)
      )
  }

  val junctionBox: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, SandboxAssets.junctionBoxMaterialOn)
      .withRef(20, 20)
      .moveTo(200, 64)

  def gameLayer(running: Seconds, currentState: SandboxGameModel, viewModel: SandboxViewModel): List[SceneGraphNode] =
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
      currentState.dude.dude.sprite.moveBy(8, 10).moveBy(viewModel.offset).withAlpha(1).withTint(RGBA.Green.withAmount(0.25)),
      currentState.dude.dude.sprite.moveBy(8, -10).withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75)),
      Clone(dudeCloneId, Depth(1), CloneTransformData.startAt(Point(16, 64)))
        .withHorizontalFlip(true)
        .withAlpha(0.5f),
      Signal.SinWave.map(theta => junctionBox.rotateTo(Radians(theta).wrap)).at(running)
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

  def uiLayer(inputState: InputState): List[SceneGraphNode] =
    List(
      Text("AB!\n!C", 2, 2, 5, SandboxAssets.fontKey).alignLeft,
      Text("AB!\n!C", 100, 2, 5, SandboxAssets.fontKey).alignCenter,
      Text("AB!\n!C", 200, 2, 5, SandboxAssets.fontKey).alignRight.onEvent {
        case (bounds, MouseEvent.Click(_, _)) =>
          if (inputState.mouse.wasMouseClickedWithin(bounds))
            println("Hit me!")
          Nil

        case _ => Nil
      }
    )

}
