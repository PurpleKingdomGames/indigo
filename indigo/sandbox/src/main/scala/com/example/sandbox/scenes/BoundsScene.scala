package com.example.sandbox.scenes

import com.example.sandbox.Fonts
import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._

object BoundsScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
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

    val graphic: Graphic[Material.Bitmap] =
      Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff)
        .moveTo(context.startUpData.viewportCenter)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val text: Text[Material.ImageEffects] =
      Text("boom!\nfish", Fonts.fontKey, SandboxAssets.fontMaterial).alignRight
        .moveTo(150, 100)
        .rotateTo(Radians.fromSeconds(context.running * speed).negative)

    val shapeBox: Shape.Box =
      Shape
        .Box(Rectangle(0, 0, 60, 30), Fill.Color(RGBA.Red), Stroke(4, RGBA.Yellow))
        .moveTo(180, 130)
        .withRef(10, 10)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val shapeCircle: Shape.Circle =
      Shape
        .Circle(Point(0), 20, Fill.Color(RGBA.Yellow), Stroke(2, RGBA.Cyan))
        .moveTo(180, 80)
        .rotateTo(Radians.fromSeconds(context.running * speed).invert)

    val shapeLine: Shape.Line =
      Shape
        .Line(Point(0), Point(30), Stroke(4, RGBA.Green))
        .moveTo(180, 10)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val shapePolygon: Shape.Polygon =
      Shape
        .Polygon(Fill.Color(RGBA.Magenta), Stroke(4, RGBA.Yellow))(
          Point(10, 10),
          Point(20, 70),
          Point(90, 90),
          Point(70, 20)
        )
        .moveTo(180, 150)
        .rotateTo(Radians.fromSeconds(context.running * speed).invert)

    val sprite: Sprite[Material.ImageEffects] =
      context.startUpData.dude.sprite
        .scaleBy(2, 2)
        .moveTo(50, 120)
        .rotateTo(Radians.fromSeconds(context.running * speed))

    val group: Group =
      Group(
        Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff),
        Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff).moveBy(15, 15)
      )
        .moveTo(200, 120)
        .rotateTo(Radians.fromSeconds(context.running * speed))
        .withRef(50, 50)

    val textBox = TextBox("Hello, World!", 100, 10).alignRight
      .withColor(RGBA.White)
      .withFontFamily(FontFamily(SandboxAssets.pixelFont.toString))
      .moveTo(100, 50)
      .rotateTo(Radians.fromSeconds(context.running * speed))
      .withRef(50, 10)

    Outcome(
      SceneUpdateFragment(
        Layer(
          Batch(
            graphic,
            Shape.Box(graphic.bounds, Fill.None, Stroke(1, RGBA.Green)),
            sprite,
            Shape.Box(
              context.findBounds(sprite).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Red)
            ),
            text,
            Shape.Box(
              context.findBounds(text).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Cyan)
            ),
            Shape.Circle(text.position, 3, Fill.None, Stroke(2, RGBA.White)),
            Shape.Circle(
              context.findBounds(text).getOrElse(Rectangle.zero).center,
              5,
              Fill.None,
              Stroke(2, RGBA.White)
            ),
            shapeBox,
            Shape.Circle(shapeBox.position, 3, Fill.None, Stroke(2, RGBA.White)),
            Shape
              .Circle(
                context.findBounds(shapeBox).getOrElse(Rectangle.zero).center,
                5,
                Fill.None,
                Stroke(2, RGBA.White)
              ),
            Shape.Box(
              context.findBounds(shapeBox).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Magenta)
            ),
            shapeCircle,
            Shape.Box(
              context.findBounds(shapeCircle).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Magenta)
            ),
            shapeLine,
            Shape.Box(
              context.findBounds(shapeLine).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Magenta)
            ),
            shapePolygon,
            Shape.Box(
              context.findBounds(shapePolygon).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Magenta)
            ),
            group,
            Shape.Box(
              context.findBounds(group).getOrElse(Rectangle.zero),
              Fill.None,
              Stroke(1, RGBA.Yellow)
            ),
            textBox,
            Shape.Box(
              textBox.bounds,
              Fill.None,
              Stroke(1, RGBA.Blue)
            )
          )
        )
      )
    )

object BoundsAssets:

  val junctionBoxMaterialOff: Material.Bitmap =
    Material.Bitmap(
      SandboxAssets.junctionBoxAlbedo,
      LightingModel.Unlit
    )
