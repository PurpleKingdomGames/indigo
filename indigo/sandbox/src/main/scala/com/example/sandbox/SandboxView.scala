package com.example.sandbox

import indigo.*

object SandboxView:

  val dudeCloneId: CloneId = CloneId("Dude")

  def updateView(
      model: SandboxGameModel,
      viewModel: SandboxViewModel,
      mouse: Mouse,
      bl: Context.Services.Bounds
  ): SceneUpdateFragment = {
    mouse.isClickedAt.headOption match {
      case Some(position) => println("Mouse clicked at: " + position.toString())
      case None           => ()
    }

    SceneUpdateFragment.empty
      .addLayer(
        LayerKey("game") ->
          Layer.Stack(
            Layer.Content(
              gameLayer(model, viewModel)
            ),
            if (viewModel.useLightingLayer)
              Layer(lightingLayer(mouse))
                .withBlending(Blending.Lighting(RGBA.White.withAlpha(0.25)))
            else
              Layer.empty,
            Layer(uiLayer(bl))
          )
      )
      .addCloneBlanks(CloneBlank(dudeCloneId, model.dude.dude.sprite))
  }

  def gameLayer(currentState: SandboxGameModel, viewModel: SandboxViewModel): Batch[SceneNode] =
    Batch(
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
        .modifyMaterial(
          _.withAlpha(1)
            .withTint(RGBA.Green.withAmount(0.25))
            .withSaturation(1.0)
        ),
      currentState.dude.dude.sprite
        .moveBy(8, -10)
        .modifyMaterial(_.withAlpha(0.5).withTint(RGBA.Red.withAmount(0.75))),
      CloneBatch(dudeCloneId, CloneBatchData(16, 64, Radians.zero, -1.0, 1.0))
    )

  def lightingLayer(mouse: Mouse): Batch[SceneNode] =
    Batch(
      Graphic(114, 64 - 20, 320, 240, SandboxAssets.lightMaterial.withTint(RGBA.Red))
        .withRef(Point(160, 120)),
      Graphic(114 - 20, 64 + 20, 320, 240, SandboxAssets.lightMaterial.withTint(RGBA.Green))
        .withRef(Point(160, 120)),
      Graphic(114 + 20, 64 + 20, 320, 240, SandboxAssets.lightMaterial.withTint(RGBA.Blue))
        .withRef(Point(160, 120)),
      Graphic(0, 0, 320, 240, SandboxAssets.lightMaterial.withTint(RGBA(1, 1, 0.0, 1)).withAlpha(1))
        .withRef(Point(160, 120))
        .moveTo(mouse.position.x, mouse.position.y)
    )

  def uiLayer(bl: Context.Services.Bounds): Batch[SceneNode] =
    Batch(
      Text("AB!\n!C", 2, 2, Fonts.fontKey, SandboxAssets.fontMaterial.withAlpha(0.5)).alignLeft,
      Text("AB!\n!C", 100, 2, Fonts.fontKey, SandboxAssets.fontMaterial.withAlpha(0.5)).alignCenter,
      Text("AB!\n\n!C", 200, 2, Fonts.fontKey, SandboxAssets.fontMaterial.withAlpha(0.5)).alignRight
        .withEventHandler {
          case (txt, MouseEvent.Click(pt)) if bl.get(txt).contains(pt) =>
            println("Clicked me!")
            None

          case _ =>
            None
        }
        .withLineHeight(20)
        .withLetterSpacing(10)
    )
