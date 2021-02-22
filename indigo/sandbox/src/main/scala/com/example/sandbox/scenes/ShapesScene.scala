package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
// import com.example.sandbox.SandboxAssets

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

    val width1: Int =
      Signal.SmoothPulse.map(d => (10 * d).toInt).at(context.running)
    val width2: Int =
      Signal.SmoothPulse.map(d => (10 * d).toInt).affectTime(0.5).at(context.running)

    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Circle(Point(50, 50), 20, CircleMaterial(RGBA.Red, RGBA.White, 3)),
          Line(StrokeMaterial(RGBA.Cyan, width1, Point(30, 10), Point(100, 50))),
          Line(StrokeMaterial(RGBA.Yellow, width2, Point(20, 60), Point(90, 10))),
          Circle(Point(100, 50), 20, CircleMaterial(RGBA.Green, RGBA.White.withAlpha(0.5), 4)),
          Circle(Point(50, 75), 10, CircleMaterial(RGBA.Blue, RGBA.Yellow, 10)),
          Circle(Point(100), 15, CircleMaterial(RGBA.Magenta, RGBA.White, 2)),
          Circle(Point(30, 75), 15, CircleMaterial(RGBA.Cyan, RGBA.White, 0)),
          Circle(Point(150), 50, CircleMaterial(RGBA.Yellow, RGBA.Black, 7)),
          Oblong(Rectangle(100, 100, 60, 30), OblongMaterial(RGBA.White, RGBA.Black, 6, 0)),
        )
    )
  }

}

final case class Foo() extends SceneEntity {
  val bounds: Rectangle = Rectangle(10, 10, 100, 100)
  val material: Material = GLSLShader(
    ShapeShaders.circleId,
    List(
      Uniform("ALPHA")        -> float(0.75),
      Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
    )
  )
  val depth: Depth      = Depth(1)
  val flip: Flip        = Flip.default
  val position: Point   = bounds.position
  val ref: Point        = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
}

object ShapeShaders {

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(circleAsset, AssetPath("assets/circle.frag")),
      AssetType.Text(lineAsset, AssetPath("assets/line.frag")),
      AssetType.Text(oblongAsset, AssetPath("assets/oblong.frag")),
      AssetType.Text(postVertAsset, AssetPath("assets/post.vert")),
      AssetType.Text(postFragAsset, AssetPath("assets/post.frag"))
    )

  val circleId: ShaderId     = ShaderId("circle external")
  val circleAsset: AssetName = AssetName("circle fragment")
  val circleExternal: CustomShader.External =
    CustomShader
      .External(circleId)
      .withFragmentProgram(circleAsset)

  val lineId: ShaderId     = ShaderId("line external")
  val lineAsset: AssetName = AssetName("line fragment")
  val lineExternal: CustomShader.External =
    CustomShader
      .External(lineId)
      .withFragmentProgram(lineAsset)

  val oblongId: ShaderId     = ShaderId("oblong external")
  val oblongAsset: AssetName = AssetName("oblong fragment")
  val oblongExternal: CustomShader.External =
    CustomShader
      .External(oblongId)
      .withFragmentProgram(oblongAsset)

  val postVertAsset: AssetName = AssetName("post vertex")
  val postFragAsset: AssetName = AssetName("post fragment")
  def postShader: CustomShader.PostExternal =
    CustomShader
      .PostExternal(ShaderId("post shader test"), StandardShaders.Bitmap)
      .withPostVertexProgram(postVertAsset)
      .withPostFragmentProgram(postFragAsset)

}
