package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

object TextureTileScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("tiling textures")

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

  def boxSize(t: Seconds): Int = Signal.SmoothPulse.map(d => (d * 64) + 64).map(_.toInt).at(t)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(
            Graphic(32, 32, Material.ImageEffects(SandboxAssets.dots))
              .withRef(16, 16)
              .moveTo(context.startUpData.viewportCenter)
              .scaleBy(fit(Vector2(32, 32), (context.startUpData.viewportCenter * 2).toVector))
              .modifyMaterial(_.withAlpha(0.2)),
            Graphic(64, 64, Material.Bitmap(SandboxAssets.dots).normal).moveTo(10, 90),
            Graphic(200, 75, Material.Bitmap(SandboxAssets.dots).tile).moveTo(10, 10),
            Graphic(50, 75, Material.Bitmap(SandboxAssets.dots).stretch).moveTo(100, 75)
          )
        )
    )

}
