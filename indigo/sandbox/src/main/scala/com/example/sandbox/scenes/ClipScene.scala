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

  def present(
      context: FrameContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Graphic(Size(128), Material.Bitmap(SandboxAssets.trafficLightsName)),
        Clip(Point(80), Size(64)),
        Shape.Box(Rectangle(Point.zero, Size(64)), Fill.None, Stroke(1, RGBA.Green)).moveTo(80, 80)
      )
    )

/*
We're going to allow you to use any material you like,
and then the plan is to override the vertex program to move to the right frame crop.
Need to add the frames as a param
pass the frames as UBO data
Write a vertex shader that expects that vertex block

Modify the default shader id - is that a problem? Maybe match on the type and build the shader? How do we get the shader in? For now, write a custom one...

frame count
start frame
arranged horizontal / vertical

start time

loop
play n times

forward
backward
ping-pong

 */
final case class Clip(position: Point, size: Size) extends EntityNode:
  // Just defaults for now
  def rotation: Radians                = Radians.zero
  def scale: Vector2                   = Vector2.one
  def depth: Depth                     = Depth.zero
  def flip: Flip                       = Flip.default
  def ref: Point                       = Point.zero
  def withDepth(newDepth: Depth): Clip = this

  def toShaderData: ShaderData =
    ShaderData(Clip.shaderId).withChannel0(SandboxAssets.trafficLightsName)

object Clip:

  val shaderId: ShaderId =
    ShaderId("clip shader")

  val vertAsset: AssetName = AssetName("clip vertex")
  val fragAsset: AssetName = AssetName("clip fragment")

  val clipShader: EntityShader.External =
    EntityShader
      .External(shaderId)
      .withVertexProgram(vertAsset)
      .withFragmentProgram(fragAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(vertAsset, AssetPath("assets/clip.vert")),
      AssetType.Text(fragAsset, AssetPath("assets/clip.frag"))
    )
