package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets

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

    val graphic: Graphic =
      Graphic(Rectangle(0, 0, 40, 40), 1, BoundsAssets.junctionBoxMaterialOff)
        .moveTo(context.startUpData.viewportCenter)
        // .withRef(20, 20)
        .rotateTo(Radians.fromSeconds(context.running * 0.5))

    Outcome(
      SceneUpdateFragment(
        Layer(
          List(
            graphic,
            Shape.Box(graphic.bounds, Fill.None, Stroke(1, RGBA.Green))
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
