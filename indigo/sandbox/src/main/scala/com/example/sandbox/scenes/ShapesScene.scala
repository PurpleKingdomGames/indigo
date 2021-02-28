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

  def updateModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] = {

    // val circleBorder: Int =
    //   Signal.SmoothPulse.map(d => 2 + (10 * d).toInt).affectTime(0.5).at(context.running)

    // val circlePosition: Point =
    //   Signal.Orbit(Point(200, 150), 10).map(_.toPoint).affectTime(0.25).at(context.running)

    // val lineThickness: Int =
    //   Signal.SmoothPulse.map(d => (10 * d).toInt).at(context.running)

    // val squareSize: Point =
    //   Point(Signal.SmoothPulse.map(d => 20 + (80 * d).toInt).affectTime(0.25).at(context.running))

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          // Shape.Circle(circlePosition, 20, Fill.Color(RGBA.Red), Stroke(circleBorder, RGBA.White)),
          // Shape.Line(Point(30, 80), Point(100, 20), Stroke(lineThickness, RGBA.Cyan)),
          // Shape.Box(Rectangle(Point(100, 100), squareSize), Fill.Color(RGBA.White), Stroke(10, RGBA.Black.withAlpha(0.75))),
          // Clone(CloneId("shape clone")).withPosition(Point(10, 10)),
          // Clone(CloneId("shape clone")).withPosition(Point(20, 10)),
          // Clone(CloneId("shape clone")).withPosition(Point(30, 10)),
          Shape
            .Polygon(Fill.RadialGradient(Point(0), RGBA.Magenta, Point(45), RGBA.Cyan), Stroke(4, RGBA.Black.withAlpha(0.75)))(
              Point(10, 10) - (Math.cos(Radians.fromSeconds(context.running).value) * 5).toInt,
              Point(20, 70) + (Math.sin(Radians.fromSeconds(context.running * Seconds(1.2)).value) * 10).toInt,
              Point(90, 90) + (Math.sin(Radians.fromSeconds(context.running * Seconds(0.8)).value) * 6).toInt,
              Point(70, 20) - (Math.cos(Radians.fromSeconds(context.running * Seconds(1.5)).value) * 8).toInt
            )
            .moveTo(175, 10)
        )
        .addCloneBlanks(CloneBlank(CloneId("shape clone"), Shape.Circle(Point.zero, 5, Fill.Color(RGBA.Green), Stroke(2, RGBA.White))))
    )
  }

}

object ShapeShaders {

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(postVertAsset, AssetPath("assets/post.vert")),
      AssetType.Text(postFragAsset, AssetPath("assets/post.frag"))
    )

  val postVertAsset: AssetName = AssetName("post vertex")
  val postFragAsset: AssetName = AssetName("post fragment")
  def postShader: Shader.PostExternal =
    Shader
      .PostExternal(ShaderId("post shader test"), StandardShaders.Bitmap)
      .withPostVertexProgram(postVertAsset)
      .withPostFragmentProgram(postFragAsset)

}
