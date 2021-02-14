package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
import com.example.sandbox.SandboxAssets

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

  def present(context: FrameContext[SandboxStartupData], model: SandboxGameModel, viewModel: SandboxViewModel): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Foo(),
          Graphic(
            32,
            32,
            StandardMaterial.PostMaterial(
              ShapeShaders.postShader.id,
              StandardMaterial.ImageEffects(SandboxAssets.dots)
            )
          )
          // Shape(
          //   0,
          //   0,
          //   64,
          //   64,
          //   1,
          //   GLSLShader(
          //     ShapeShaders.externalCircleId,
          //     List(
          //       Uniform("ALPHA")        -> float(0.75),
          //       Uniform("BORDER_COLOR") -> vec3(1.0, 1.0, 0.0)
          //     )
          //   )
          // ).moveTo(context.startUpData.viewportCenter - Point(32, 32))
        )
    )

}

final case class Foo() extends SceneEntity {
  val bounds: Rectangle = Rectangle(10, 10, 100, 100)
  val material: Material = GLSLShader(
    ShapeShaders.externalCircleId,
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

  val externalCircleId: ShaderId =
    ShaderId("circle external")

  val vertAsset: AssetName = AssetName("circle vertex")
  val fragAsset: AssetName = AssetName("circle fragment")

  val circleExternal: CustomShader.External =
    CustomShader
      .External(externalCircleId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)
      .withLightProgram(fragAsset)

  val postVertAsset: AssetName = AssetName("post vertex")
  val postFragAsset: AssetName = AssetName("post fragment")

  def postShader: CustomShader.PostExternal =
    CustomShader
      .PostExternal(ShaderId("post shader test"), StandardShaders.Bitmap)
      .withPostVertexProgram(postVertAsset)
      .withPostFragmentProgram(postFragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/circle.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/circle.frag")),
      AssetType.Text(postVertAsset, AssetPath("assets/post.vert")),
      AssetType.Text(postFragAsset, AssetPath("assets/post.frag"))
    )

}
