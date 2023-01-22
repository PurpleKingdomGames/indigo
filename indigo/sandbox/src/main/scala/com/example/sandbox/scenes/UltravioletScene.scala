package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxView
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
import indigo._
import indigo.scenes._
import indigo.shared.shader.UltravioletShader
import indigo.shared.shader.library.IndigoUV
import indigo.shared.shader.library.WebGL2Base
import ultraviolet.datatypes.ShaderResult

object UltravioletScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("ultraviolet")

  def subSystems: Set[SubSystem] =
    Set()

  def updateModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel
  ): GlobalEvent => Outcome[SandboxGameModel] =
    _ => Outcome(model)

  def updateViewModel(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): GlobalEvent => Outcome[SandboxViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[SandboxStartupData],
      model: SandboxGameModel,
      viewModel: SandboxViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          Layer(
            UVEntity(140, 50, 32, 32, Depth.zero, ShaderData(UVShaders.circleId))
          )
        )
    )

}

final case class UVEntity(x: Int, y: Int, width: Int, height: Int, depth: Depth, shader: ShaderData)
    extends EntityNode[UVEntity]:
  val flip: Flip                    = Flip.default
  val position: Point               = Point(x, y)
  val size: Size                    = Size(width, height)
  val ref: Point                    = Point.zero
  val rotation: Radians             = Radians.zero
  val scale: Vector2                = Vector2.one
  lazy val toShaderData: ShaderData = shader

  def withDepth(newDepth: Depth): UVEntity =
    this.copy(depth = newDepth)

  val eventHandlerEnabled: Boolean                                   = false
  def eventHandler: ((UVEntity, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object UVShaders:

  import ultraviolet.syntax.*

  val circleId: ShaderId =
    ShaderId("uv circle")

  val circle: UltravioletShader =
    new UltravioletShader {

      val id: ShaderId = circleId

      inline def modifyVertex: vec4 => Shader[IndigoUV.IndigoVertexEnv, vec4] =
        (vertex: vec4) => Shader[IndigoUV.IndigoVertexEnv, vec4](_ => vertex)

      inline def circleSdf = (p: vec2, r: Float) => length(p) - r

      inline def calculateColour = (uv: vec2, sdf: Float) =>
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      inline def modifyColor: vec4 => ultraviolet.syntax.Shader[IndigoUV.IndigoFragmentEnv, vec4] =
        _ =>
          Shader[IndigoUV.IndigoFragmentEnv, vec4] { env =>
            val sdf = circleSdf(env.UV - 0.5f, 0.5f)
            calculateColour(env.UV, sdf)
          }

      val vertex: ShaderResult =
        WebGL2Base.vertex(modifyVertex)

      val fragment: ShaderResult =
        WebGL2Base.fragment(modifyColor)
    }
