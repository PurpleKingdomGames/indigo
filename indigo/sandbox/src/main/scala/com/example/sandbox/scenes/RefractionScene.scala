package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import indigoextras.effectmaterials.Refraction
import indigoextras.effectmaterials.RefractionEntity

object RefractionScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("refraction")

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

  val graphic: Graphic[Material.Bitmap] =
    Graphic(Rectangle(0, 0, 64, 64), SandboxAssets.junctionBoxMaterial)
      .withRef(20, 20)

  val imageLight: Graphic[Material.Bitmap] =
    Graphic(Rectangle(0, 0, 320, 240), SandboxAssets.imageLightMaterial)
      .moveBy(-14, -60)

  val distortion: Graphic[RefractionEntity] =
    Graphic(Rectangle(0, 0, 240, 240), SandboxAssets.normalMapMaterial)
      .scaleBy(0.5, 0.5)
      .withRef(120, 120)

  val background: Graphic[Material.Bitmap] =
    Graphic(Rectangle(0, 0, 790, 380), SandboxAssets.foliageMaterial)

  def sliding: Signal[Graphic[RefractionEntity]] =
    Signal.SmoothPulse.map { d =>
      distortion.moveTo(Point(70, 70 + (50 * d).toInt))
    }

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
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
            distortion.moveTo(viewCenter + Point(50, 0)),
            sliding.affectTime(0.3).at(context.frame.time.running)
          ).withBlending(
            Refraction.blending(
              Signal.SmoothPulse
                .map(d => 0.25 * d)
                .affectTime(0.25)
                .at(context.frame.time.running)
            )
          )
        )
    )
  }

}
