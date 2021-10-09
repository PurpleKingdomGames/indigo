package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel

object ShapesScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("shapes")

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
  ): Outcome[SceneUpdateFragment] = {

    val circleGradient: Int =
      Signal.SmoothPulse.map(d => 2 + (10 * d).toInt).at(context.running)

    val circlePosition: Point =
      Signal.Orbit(Point(200, 150), 10).map(_.toPoint).affectTime(0.25).at(context.running)

    val lineThickness: Int =
      Signal.SmoothPulse.map(d => (10 * d).toInt).at(context.running)

    val squareSize: Size =
      val signal = Signal.SmoothPulse.map(d => (100 * d).toInt).affectTime(0.25).at(context.running)

      Size(
        signal,
        99 - signal
      )

    val blue =
      Shape
        .Box(Rectangle(0, 0, 24, 60), Fill.Color(RGBA.Blue.withAlpha(0.5)), Stroke(10, RGBA.Blue.withAlpha(0.5)))
        .moveTo(100, 100)

    val red =
      Shape
        .Box(Rectangle(0, 0, 60, 24), Fill.Color(RGBA.Red.withAlpha(0.5)), Stroke(10, RGBA.Red.withAlpha(0.5)))
        .moveTo(100, 100)

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Shape.Circle(
            circlePosition,
            20,
            Fill.RadialGradient(Point(20), RGBA.Red.withAlpha(0.25), Point.zero - circleGradient, RGBA.Red),
            Stroke(1, RGBA.Red)
          ),
          Shape.Line(Point(30, 80), Point(100, 20), Stroke(lineThickness, RGBA.Cyan)),
          Shape
            .Box(
              Rectangle(Point(100, 100), squareSize),
              Fill.Color(RGBA.White),
              Stroke(11, RGBA.Black.withAlpha(0.75))
            )
            .withRef(squareSize.toPoint / 2),
          CloneBatch(CloneId("shape clone"), CloneBatchData(10, 10)),
          CloneBatch(CloneId("shape clone"), CloneBatchData(20, 10)),
          CloneBatch(CloneId("shape clone"), CloneBatchData(30, 10)),
          Shape
            .Polygon(
              Fill.LinearGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan),
              Stroke(4, RGBA.Black.withAlpha(0.75))
            )(
              Point(10, 10) - (Math.cos(Radians.fromSeconds(context.running).toDouble) * 5).toInt,
              Point(20, 70) + (Math.sin(Radians.fromSeconds(context.running * Seconds(1.2)).toDouble) * 10).toInt,
              Point(90, 90) + (Math.sin(Radians.fromSeconds(context.running * Seconds(0.8)).toDouble) * 6).toInt,
              Point(70, 20) - (Math.cos(Radians.fromSeconds(context.running * Seconds(1.5)).toDouble) * 8).toInt
            )
            .moveTo(175, 10),
          blue,
          Shape.Box(
            blue.calculatedBounds(context.boundaryLocator),
            Fill.None,
            Stroke(1, RGBA.Blue)
          ), //outline blue
          red,
          Shape.Box(
            red.calculatedBounds(context.boundaryLocator),
            Fill.None,
            Stroke(1, RGBA.Red)
          ), //outline red
          Shape
            .Box(Rectangle(0, 0, 100, 100), Fill.Color(RGBA.Green.withAlpha(0.5)), Stroke.None)
        )
        .addCloneBlanks(
          CloneBlank(CloneId("shape clone"), Shape.Circle(Point.zero, 5, Fill.Color(RGBA.Green), Stroke(2, RGBA.White)))
        )
    )
  }

}
