package com.example.sandbox.scenes

import com.example.sandbox.Log
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._
import indigo.shared.geometry.Polygon
import indigo.shared.geometry.Vertex
import indigoextras.ui.HitArea

object UiScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("ui")

  def subSystems: Set[SubSystem] =
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
    case FrameTick =>
      viewModel.update(context.mouse)

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
      viewModel: SandboxViewModel
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
        viewModel.button.draw
      )
    )

}
