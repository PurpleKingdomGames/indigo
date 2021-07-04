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
            TilingTexture(10, 10, 200, 100)
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
