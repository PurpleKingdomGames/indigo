package com.example.sandbox.scenes

import com.example.sandbox.Log
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.ui.simple.Button
import indigoextras.ui.simple.ButtonAssets
import indigoextras.ui.simple.HitArea

object UiScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = UiSceneViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, UiSceneViewModel] =
    Lens(
      _.uiScene,
      (m, vm) => m.copy(uiScene = vm)
    )

  def name: SceneName =
    SceneName("ui")

  def subSystems: Set[SubSystem[SandboxGameModel]] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: UiSceneViewModel
  ): GlobalEvent => Outcome[UiSceneViewModel] =
    case FrameTick =>
      viewModel.update(context.frame.input.mouse, context.frame.input.pointers)

    case Log(msg) =>
      println(msg)
      Outcome(viewModel)

    case _ =>
      Outcome(viewModel)

  val points: Batch[Point] =
    Batch(
      Point(10, 10),
      Point(20, 70),
      Point(90, 90),
      Point(70, 20)
    )

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: UiSceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Shape
          .Polygon(
            points,
            Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan),
            Stroke(4, RGBA.Black.withAlpha(0.75))
          )
          .moveTo(175, 10),
        viewModel.button1.draw,
        viewModel.button2.draw,
        viewModel.button3.draw
      )
    )

}

final case class UiSceneViewModel(
    hitArea: HitArea,
    button1: Button,
    button2: Button,
    button3: Button
):
  def update(mouse: Mouse, pointers: Pointers): Outcome[UiSceneViewModel] =
    for {
      ha  <- hitArea.update(pointers)
      bn1 <- button1.update(pointers)
      bn2 <- button2.update(pointers)
      bn3 <- button3.update(pointers)
    } yield this.copy(hitArea = ha, button1 = bn1, button2 = bn2, button3 = bn3)

object UiSceneViewModel:

  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 16, 16, Material.Bitmap(AssetName("dots"))).withCrop(0, 0, 16, 16),
      over = Graphic(0, 0, 16, 16, Material.Bitmap(AssetName("dots"))).withCrop(16, 0, 16, 16),
      down = Graphic(0, 0, 16, 16, Material.Bitmap(AssetName("dots"))).withCrop(16, 16, 16, 16)
    )

  val initial: UiSceneViewModel =
    UiSceneViewModel(
      HitArea(Polygon.Closed(UiScene.points.map(Vertex.fromPoint)))
        .moveTo(175, 10)
        .withUpActions(Log("Up!"))
        .withClickActions(Log("Click!"))
        .withDownActions(Log("Down!"))
        .withHoverOverActions(Log("Over!"))
        .withHoverOutActions(Log("Out!"))
        .withHoldDownActions(Log("Hold down!")),
      Button(
        buttonAssets = buttonAssets,
        bounds = Rectangle(0, 0, 16, 16)
      )
        .withUpActions(Log("Up! 1"))
        .withClickActions(Log("Click! 1"))
        .withDownActions(Log("Down! 1"))
        .withHoverOverActions(Log("Over! 1"))
        .withHoverOutActions(Log("Out! 1"))
        .withHoldDownActions(Log("Hold down! 1"))
        .moveTo(16, 16),
      Button(
        buttonAssets = buttonAssets,
        bounds = Rectangle(0, 0, 16, 16)
      )
        .withUpActions(Log("Up! 2"))
        .withClickActions(Log("Click! 2"))
        .withDownActions(Log("Down! 2"))
        .withHoverOverActions(Log("Over! 2"))
        .withHoverOutActions(Log("Out! 2"))
        .withHoldDownActions(Log("Hold down! 2"))
        .moveTo(48, 16),
      Button(
        buttonAssets = buttonAssets,
        bounds = Rectangle(0, 0, 16, 16)
      )
        .withUpActions(Log("Up! 3"))
        .withClickActions(Log("Click! 3"))
        .withDownActions(Log("Down! 3"))
        .withHoverOverActions(Log("Over! 3"))
        .withHoverOutActions(Log("Out! 3"))
        .withHoldDownActions(Log("Hold down! 3"))
        .moveTo(80, 16)
    )
