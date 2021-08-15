package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets

object CameraScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("camera")

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

  def zoom: Signal[Zoom] =
    Signal.SmoothPulse.map { d =>
      Zoom(d * 2.0d)
    }

  def orbit: Signal[Point] =
    Signal.Orbit(Point.zero, 100.0d).map(_.toPoint)

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter

    Outcome(
      SceneUpdateFragment(
        Layer(
          Graphic(
            Rectangle(Point.zero, (context.startUpData.viewportCenter * 4).toSize),
            1,
            SandboxAssets.foliageMaterial
          )
        ).withMagnification(1),
        Layer(
          Graphic(32, 32, Material.Bitmap(SandboxAssets.dots)).moveTo(context.startUpData.viewportCenter - Point(16))
        ).withCamera(Camera.default) // Override scene camera, so this layer doesn't move.
      ).modifyCamera(
        _.moveTo(orbit.at(context.running * 0.3))
          .withZoom(zoom.at(context.running * 0.35))
      )
    )
  }

}
