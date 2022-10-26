package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxView
import com.example.sandbox.SandboxViewModel
import indigo.ShaderPrimitive._
import indigo._
import indigo.scenes._

object ShadersScene extends Scene[SandboxStartupData, SandboxGameModel, SandboxViewModel] {

  type SceneModel     = SandboxGameModel
  type SceneViewModel = SandboxViewModel

  def eventFilters: EventFilters =
    EventFilters.Restricted

  def modelLens: Lens[SandboxGameModel, SandboxGameModel] =
    Lens.keepOriginal

  def viewModelLens: Lens[SandboxViewModel, SandboxViewModel] =
    Lens.keepOriginal

  def name: SceneName =
    SceneName("custom shaders")

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
      SceneUpdateFragment(
        Layer(
          CustomShader(
            0,
            0,
            100,
            100,
            Depth.zero,
            ShaderData(CustomShader.shaderId)
          )
        )
      )
    )

}

final case class CustomShader(x: Int, y: Int, width: Int, height: Int, depth: Depth, shader: ShaderData)
    extends EntityNode[CustomShader]:
  val flip: Flip                    = Flip.default
  val position: Point               = Point(x, y)
  val size: Size                    = Size(width, height)
  val ref: Point                    = Point.zero
  val rotation: Radians             = Radians.zero
  val scale: Vector2                = Vector2.one
  lazy val toShaderData: ShaderData = shader

  def withDepth(newDepth: Depth): CustomShader =
    this.copy(depth = newDepth)

  val eventHandlerEnabled: Boolean                                       = false
  def eventHandler: ((CustomShader, GlobalEvent)) => Option[GlobalEvent] = Function.const(None)

object CustomShader:
  import indigo.macroshaders.Shader
  import indigo.macroshaders.IndigoEntityFragment as FragEnv
  import indigo.macroshaders.ShaderDSL.*

  val shaderId: ShaderId =
    ShaderId("custom shader")

  inline def fragment1: Shader[FragEnv, rgba] =
    Shader { env =>
      val zero  = 0.0f
      val alpha = 1.0f
      rgba(env.UV, zero, alpha)
    }

  inline def circleSdf(p: vec2, r: Float): Shader[FragEnv, Float] =
    Shader(length(p) - r)

  inline def calculateColour(uv: vec2, sdf: Float): Shader[FragEnv, rgba] =
    Shader {
      val fill       = rgba(uv, 0.0f, 1.0f)
      val fillAmount = (1.0f - step(0.0f, sdf)) * fill.a
      rgba(fill.rgb * fillAmount, fillAmount)
    }

  inline def fragment2: Shader[FragEnv, rgba] =
    Shader.ask[FragEnv].flatMap { env =>
      for {
        sdf    <- circleSdf(env.UV - 0.5f, 0.5f)
        colour <- calculateColour(env.UV, sdf)
      } yield colour
    }

  println(fragment2.toGLSL)

  val shader: EntityShader.Source =
    EntityShader
      .Source(shaderId)
      .withFragmentProgram(fragment1.toGLSL)
