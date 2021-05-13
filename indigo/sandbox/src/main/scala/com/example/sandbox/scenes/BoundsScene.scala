package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets
import com.example.sandbox.Fonts

object BoundsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("bounds")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =

    val speed = 0.25

    val graphic: Graphic =
      Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff)
        .moveTo(context.startUpData.viewportCenter)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val text: Text =
      Text("Hello world", Fonts.fontKey, SandboxAssets.fontMaterial).alignCenter
        .moveTo(30, 30)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val shape: Shape.Box =
      Shape
        .Box(Rectangle(0, 0, 100, 50), Fill.Color(RGBA.Red))
        .moveTo(200, 30)
        .withRef(10, 20)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val sprite: Sprite =
      context.startUpData.dude.sprite
        .moveTo(50, 120)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val group: Group =
      Group(
        Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff),
        Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff).moveBy(15, 15)
      )
        .moveTo(200, 120)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val textBox = TextBox("Hello, World!", 100, 10).alignRight
      .withColor(RGBA.White)
      .withFontFamily(FontFamily(SandboxAssets.pixelFont.toString))
      .moveTo(100, 50)
      .rotateTo(Radians.fromSeconds(context.running * speed))
      .withRef(50, 10)

    Outcome(
      SceneUpdateFragment(
        Layer(
          List(
            graphic,
            Shape.Box(graphic.bounds, Fill.None, Stroke(1, RGBA.Green)),
            sprite,
            Shape.Box(
              sprite.calculatedBounds(context.boundaryLocator).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Red)
            ),
            // text,
            // Shape.Box(
            //   text.calculatedBounds(context.boundaryLocator).getOrElse(Rectangle.zero),
            //   Fill.None,
            //   Stroke(1, RGBA.Cyan)
            // ),
            // shape,
            // Shape.Box(shape.bounds, Fill.None, Stroke(1, RGBA.Magenta)),
            // group,
            // Shape.Box(
            //   group.calculatedBounds(context.boundaryLocator).getOrElse(Rectangle.zero),
            //   Fill.None,
            //   Stroke(1, RGBA.Yellow)
            // ),
            textBox,
            Shape.Box(
              textBox.calculatedBounds(context.boundaryLocator).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Blue)
            )
          )
        )
      )
    )

}

object BoundsAssets {

  val junctionBoxMaterialOff: Material.Bitmap =
    Material.Bitmap(
      SandboxAssets.junctionBoxAlbedo,
      LightingModel.Unlit
    )

}
