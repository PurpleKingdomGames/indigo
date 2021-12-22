package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo._
import indigo.scenes._

object ClipScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel]:

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepLatest

  def name: SceneName =
    SceneName("clips")

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
    Outcome(
      SceneUpdateFragment(
        Graphic(Size(128), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Clip(Point(80, 80), Size(64), ClipSheet(3, Seconds(0.25), 2), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(80, 80)),
        Clip(Point(144, 80), Size(64), ClipSheet(3, Seconds(0.5), 2), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(144, 80))
      )
    )
