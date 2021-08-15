package com.example.sandbox.scenes

import indigo._
import indigo.scenes._

import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxViewModel
import com.example.sandbox.SandboxAssets
import indigo.shared.shader.ShaderPrimitive.{array, vec4, float}

object AnimatedGraphicScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def modelLens: indigo.scenes.Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("animated graphic")

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
    case _ =>
      Outcome(viewModel)

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        AnimatedGraphic
          .fromAnimation(SandboxAssets.dudeName, model.dude.dude.animations, Option(CycleLabel("walk right")))
          .toList
      )
    )

}

final case class AnimatedGraphic(asset: AssetName, frames: NonEmptyList[Frame]) extends EntityNode {
  val flip: Flip        = Flip.default
  val bounds: Rectangle = Rectangle(0, 0, 64, 64)
  val position: Point   = bounds.position
  val size: Size        = bounds.size
  val ref: Point        = Point.zero
  val rotation: Radians = Radians.zero
  val scale: Vector2    = Vector2.one
  val depth: Depth      = Depth(1)

  def withDepth(newDepth: Depth): AnimatedGraphic = this

  val toShaderData: ShaderData =
    ShaderData(AnimatedGraphic.shaderId)
      .withChannel0(asset)
      .withUniformBlocks(
        UniformBlock(
          "AnimatedGraphic",
          List(
            Uniform("FRAME_COUNT") -> float(frames.length.toDouble),
            Uniform("FRAMES") -> array(
              frames.length,
              frames.toList
                .map(f => vec4(f.crop.x.toFloat, f.crop.y.toFloat, f.crop.width.toFloat, f.crop.height.toFloat))
            ),
            Uniform("DURATIONS") -> array(
              frames.length,
              frames.toList.map(f => float(f.duration.toLong.toDouble))
            )
          )
        )
      )
}

object AnimatedGraphic {

  def fromAnimation(asset: AssetName, animation: Animation, cycleLabel: Option[CycleLabel]): Option[AnimatedGraphic] =
    cycleLabel match
      case None =>
        Option(fromCycle(asset, animation.cycles.head))

      case Some(label) =>
        animation.cycles
          .find(_.label == label)
          .map(fromCycle(asset, _))

  def fromCycle(asset: AssetName, cycle: Cycle): AnimatedGraphic =
    AnimatedGraphic(asset, cycle.frames)

  val shaderId: ShaderId =
    ShaderId("animated graphic")

  val fragAsset: AssetName = AssetName("animated graphic fragment")

  val shader: EntityShader.External =
    EntityShader
      .External(shaderId)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(fragAsset, AssetPath("assets/animated.frag"))
    )

}
