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
        Layer(
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
          //
          Clip(
            Point(64, 160),
            Size(64),
            ClipSheet(3, Seconds(0.5), 2),
            Material.Bitmap(SandboxAssets.trafficLightsName)
          ).smoothPingPong,
          Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(64, 160)),
          label("Smooth Ping-Pong (500ms)").moveTo(64, 64 + 160),
          // Pirate
          Clip(
            Point(128, 0),
            Size(96, 96),
            ClipSheet(37, Millis(100).toSeconds, 10)
              .withArrangement(indigo.shared.scenegraph.ClipSheetArrangement.Vertical),
            SandboxAssets.captainMaterial
          ).withStartOffset(29),
          Shape.Box(Rectangle(Point.zero, Size(96)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(128, 0)),
          // Pirate
          Clip(
            Point(224, 0),
            Size(96, 96),
            ClipSheet(4, Millis(200).toSeconds, 10)
              .withArrangement(indigo.shared.scenegraph.ClipSheetArrangement.Vertical),
            SandboxAssets.captainMaterial
          ).play(startTime = Seconds(5.0), numOfTimes = 9)
          .withStartOffset(17),
          Shape.Box(Rectangle(Point.zero, Size(96)), Fill.None, Stroke(1, RGBA.Green)).moveTo(Point(224, 0)),
          label("Start after 5 seconds").moveTo(224 + 96 + 2, 0),
          label("Play 9 times and stop").moveTo(224 + 96 + 2, 10),
          label("Half speed").moveTo(224 + 96 + 2, 20),
          label("First frame is white").moveTo(224 + 96 + 2, 30),
        ).withMagnification(1)
      )
    )
