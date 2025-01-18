package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object NineSliceScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("nine slice scene")

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
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def fit(originalSize: Vector2, screenSize: Vector2): Vector2 =
    Vector2(Math.max(screenSize.x / originalSize.x, screenSize.y / originalSize.y))

  def boxSize(t: Seconds): Int = Signal.SmoothPulse.map(d => (d * 64) + 32).map(_.toInt).at(t)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
    val boxSizeValue: Int = boxSize(context.frame.time.running)

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(
            Graphic(
              boxSizeValue,
              boxSizeValue,
              Material.Bitmap(SandboxAssets.nineSlice).nineSlice(Rectangle(16, 16, 32, 32))
            ).moveTo(5, 5),
            // Shape
            //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
            //   .moveTo(5, 5),
            Graphic(
              boxSizeValue,
              boxSizeValue,
              Material.Bitmap(SandboxAssets.platform).nineSlice(Rectangle(8, 20, 112, 40))
            ).moveTo(100, 5),
            // Shape
            //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
            //   .moveTo(75, 5),
            Graphic(
              boxSizeValue,
              boxSizeValue,
              Material.Bitmap(SandboxAssets.window).nineSlice(Rectangle(3, 15, 121, 41))
            ).moveTo(5, 100)
            // Shape
            //   .Box(Rectangle(boxSizeValue, boxSizeValue), Fill.None, Stroke(1, RGBA.Green))
            //   .moveTo(5, 75),
          )
        )
    )
  }

}
