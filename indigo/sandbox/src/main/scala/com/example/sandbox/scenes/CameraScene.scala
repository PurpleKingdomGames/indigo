package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._

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
      Zoom(0.8 + (d * 0.2))
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
          ),
          Graphic(32, 32, Material.Bitmap(SandboxAssets.dots)).moveTo(-16, -16)
        ).withMagnification(1),
        Layer(
          Graphic(32, 32, Material.ImageEffects(SandboxAssets.dots).withAlpha(0.4))
            .moveTo(context.startUpData.viewportCenter - Point(16))
        ).withCamera(Camera.default) // Override scene camera, so this layer doesn't move.
      ).modifyCamera {
        case c: Camera.Fixed =>
          c.toLookAt
            .lookAt(context.mouse.position)
            .rotateBy(Radians.fromSeconds(context.running * 0.2))
            .withZoom(Zoom(0.75))

        case c =>
          c
        // _.moveTo(orbit.at(context.running * 0.3))
        // .withZoom(zoom.at(context.running * 0.35))
      }
    )
  }

}
