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

    val circleBorder: Int =
      Signal.SmoothPulse.map(d => 2 + (10 * d).toInt).affectTime(0.5).at(context.running)

    val circlePosition: Point =
      Signal.Orbit(Point(200, 150), 10).map(_.toPoint).affectTime(0.25).at(context.running)

    val lineThickness: Int =
      Signal.SmoothPulse.map(d => (10 * d).toInt).at(context.running)

    val squareSize: Point =
      Point(Signal.SmoothPulse.map(d => 20 + (80 * d).toInt).affectTime(0.25).at(context.running))

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Shape.Circle(circlePosition, 20, RGBA.Red, RGBA.White, circleBorder),
          Shape.Line(Point(30, 80), Point(100, 20), RGBA.Cyan, lineThickness),
          Shape.Box(Rectangle(Point(100, 100), squareSize), RGBA.White, RGBA.Black.withAlpha(0.75), 10),
          Clone(CloneId("shape clone")).withPosition(Point(10, 10)),
          Clone(CloneId("shape clone")).withPosition(Point(20, 10)),
          Clone(CloneId("shape clone")).withPosition(Point(30, 10)),
          Shape.Polygon(RGBA.Green, RGBA.White.withAlpha(0.75), 10)(
            Point(10, 10),
            Point(20, 70),
            Point(90, 90)
          )
        )
        .addCloneBlanks(CloneBlank(CloneId("shape clone"), Shape.Circle(Point.zero, 5, RGBA.Green, RGBA.White, 2)))
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
