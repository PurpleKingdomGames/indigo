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

  val label: String => TextBox =
    TextBox(_)
      .withFontSize(Pixels(8))
      .withColor(RGBA.White)
      .withFontFamily(FontFamily.uiMonospace)

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        //
        Clip(Point(0), Size(64), ClipSheet(3, Seconds(0.25), 2), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(0)),
        label("Forwards (250ms)").moveTo(0, 64),
        //
        Clip(Point(0, 80), Size(64), ClipSheet(3, Seconds(0.25), 2), Material.Bitmap(SandboxAssets.trafficLightsName))
          .withPlayMode(ClipPlayMode.Loop(ClipPlayDirection.Backward)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(0, 80)),
        label("Backwards (250ms)").moveTo(0, 64 + 80),
        //
        Clip(Point(64, 0), Size(64), ClipSheet(3, Seconds(0.5), 2), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(64, 0)),
        label("Forwards (500ms)").moveTo(64, 64),
        //
        Clip(Point(64, 80), Size(64), ClipSheet(3, Seconds(0.5), 2), Material.Bitmap(SandboxAssets.trafficLightsName))
          .withPlayMode(ClipPlayMode.Loop(ClipPlayDirection.PingPong)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(64, 80)),
        label("Ping-Pong (500ms)").moveTo(64, 64 + 80),
        // Pirate
        Clip(
          Point(128, 0),
          Size(96, 96),
          ClipSheet(4, Millis(100).toSeconds, 10)
            .withArrangement(indigo.shared.scenegraph.ClipSheetArrangement.Vertical)
            .withStartOffset(29),
          SandboxAssets.captainMaterial
        ),
        Shape.Box(Rectangle(Point.zero, Size(96)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(128, 0))
      )
    )
