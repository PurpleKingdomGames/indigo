package com.example.sandbox.scenes

import indigo._
import indigo.scenes._
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets
import indigoextras.effectmaterials.Refraction

object TextureTileScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("tiling textures")

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

  def fit(originalSize: Vector2, screenSize: Vector2): Vector2 =
    Vector2(Math.max(screenSize.x / originalSize.x, screenSize.y / originalSize.y))

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = context.startUpData.viewportCenter

    Outcome(
      SceneUpdateFragment.empty
        .addLayers(
          Layer(
            Graphic(32, 32, Material.Bitmap(SandboxAssets.dots))
              .withRef(16, 16)
              .moveTo(context.startUpData.viewportCenter)
              .scaleBy(fit(Vector2(32, 32), (context.startUpData.viewportCenter * 2).toVector)),
            TilingTexture (10, 10, 200, 75),
            StretchToFit(100, 75, 50, 75)
          )
        )
    )
  }

}

final case class TilingTexture(x: Int, y: Int, width: Int, height: Int) extends EntityNode {
  val flip: Flip        = Flip.default
  val bounds: Rectangle = Rectangle(x, y, width, height)
  val position: Point   = bounds.position
  val size: Size        = bounds.size
  val ref: Point        = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val depth: Depth      = Depth(1)

  def withDepth(newDepth: Depth): TilingTexture = this

  val toShaderData: ShaderData =
    ShaderData(TilingTexture.shaderId)
      .withChannel0(SandboxAssets.dots)
}

object TilingTexture {

  val shaderId: ShaderId =
    ShaderId("tiling")

  val fragAsset: AssetName = AssetName("tiling fragment")

  val tilingShader: EntityShader.External =
    EntityShader
      .External(shaderId)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(fragAsset, AssetPath("assets/tiling.frag"))
    )

}

final case class StretchToFit(x: Int, y: Int, width: Int, height: Int) extends EntityNode {
  val flip: Flip        = Flip.default
  val bounds: Rectangle = Rectangle(x, y, width, height)
  val position: Point   = bounds.position
  val size: Size        = bounds.size
  val ref: Point        = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val depth: Depth      = Depth(1)

  def withDepth(newDepth: Depth): StretchToFit = this

  val toShaderData: ShaderData =
    ShaderData(StretchToFit.shaderId)
      .withChannel0(SandboxAssets.dots)
}

object StretchToFit {

  val shaderId: ShaderId =
    ShaderId("stretch to fit")

  val fragAsset: AssetName = AssetName("stretch to fit fragment")

  val stretchShader: EntityShader.External =
    EntityShader
      .External(shaderId)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(fragAsset, AssetPath("assets/stretch.frag"))
    )

}
