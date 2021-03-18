package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets

object RefractionScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("refraction")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, SandboxAssets.junctionBoxMaterial)
      .withRef(20, 20)

  val imageLight: Graphic =
    Graphic(Rectangle(0, 0, 320, 240), 1, SandboxAssets.imageLightMaterial)
      .moveBy(-14, -60)

  val distortion: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, SandboxAssets.smoothBumpMaterial)
      .withRef(32, 32)

  val background: Graphic =
    Graphic(Rectangle(0, 0, 790, 380), 1, SandboxAssets.foliageMaterial)

  def orbiting(distance: Int, around: Point): Signal[Graphic] =
    Signal.Orbit(around, distance.toDouble).map { vec =>
      distortion.moveTo(vec.toPoint)
    }

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(
            background,
            graphic.moveTo(viewCenter),
            graphic.moveTo(viewCenter).moveBy(-60, 0).withMaterial(SandboxAssets.junctionBoxMaterial),
            graphic.moveTo(viewCenter).moveBy(-30, 0).withMaterial(SandboxAssets.junctionBoxMaterial),
            graphic.moveTo(viewCenter).moveBy(30, 0).withMaterial(SandboxAssets.junctionBoxMaterial),
            graphic.moveTo(viewCenter).moveBy(60, 0).withMaterial(SandboxAssets.junctionBoxMaterial)
          ).withMagnification(2),
          Layer(imageLight)
            .withBlending(Blending.Lighting(RGBA(0.2, 0.5, 0.3, 0.5))),
          Layer(
            distortion.moveTo(viewCenter),
            orbiting(40, viewCenter).affectTime(0.25).at(context.gameTime.running)
          )
        )
    )
  }

}
