package com.example.sandbox.scenes

import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*
import ultraviolet.syntax.*

import scala.annotation.nowarn

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

  def subSystems: Set[SubSystem[SandboxGameModel]] =
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
            BlankEntity(10, 10, 150, 150, ShaderData(UVShaders.voronoiId)),
            BlankEntity(140, 50, 32, 32, ShaderData(UVShaders.circleId))
          )
        )
        .withBlendMaterial(MakeRedBlend)
    )

}

case object MakeRedBlend extends BlendMaterial derives CanEqual {
  lazy val toShaderData: ShaderData =
    ShaderData(UVShaders.redBlendId)
}

object UVShaders:

  // Blend

  @nowarn("msg=unused")
  inline def makeRedder: Shader[BlendFragmentEnv, Unit] =
    Shader[BlendFragmentEnv] { env =>
      def fragment(color: vec4): vec4 =
        val amount = abs(sin(env.TIME))
        vec4(color.rgb * vec3(1.0, amount, amount), color.a)
    }

  val redBlendId: ShaderId =
    ShaderId("red blend")

  val redBlend: UltravioletShader =
    UltravioletShader.blendFragment(
      redBlendId,
      BlendShader.fragment(makeRedder, BlendFragmentEnv.reference)
    )

  // Circle - Entity

  @nowarn("msg=unused")
  inline def orbitVertex: Shader[VertexEnv, Unit] =
    Shader[VertexEnv] { env =>

      def vertex(v: vec4): vec4 =
        vec4(v.x + sin(env.TIME * 0.5f), v.y + cos(env.TIME * 0.5f), v.z, v.w)

    }

  @nowarn("msg=unused")
  inline def modifyCircleColor: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>

      def circleSdf(p: vec2, r: Float): Float =
        length(p) - r

      def calculateColour(uv: vec2, sdf: Float): vec4 =
        val fill       = vec4(uv, 0.0f, 1.0f)
        val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
        vec4(fill.xyz * fillAmount, fillAmount)

      def fragment(color: vec4): vec4 =
        val sdf = circleSdf(env.UV - 0.5f, 0.5f)
        calculateColour(env.UV, sdf)

    }

  @nowarn("msg=unused")
  inline def prepare: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { _ =>
      def prepare: Unit = ()
    }

  @nowarn("msg=unused")
  inline def light: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { _ =>
      def light: Unit = ()
    }

  @nowarn("msg=unused")
  inline def composite: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { _ =>
      def composite: Unit = ()
    }

  val circleId: ShaderId =
    ShaderId("uv circle")

  val circle: UltravioletShader =
    UltravioletShader(
      circleId,
      EntityShader.vertex(orbitVertex, VertexEnv.reference),
      EntityShader.fragment(modifyCircleColor, prepare, light, composite, FragmentEnv.reference)
    )

  // Voronoi - Entity

  val voronoiId = ShaderId("uv voronoi")

  @nowarn("msg=unused")
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def modifyColor: Shader[FragmentEnv, Unit] =
    Shader[FragmentEnv] { env =>

      def N22(p: vec2): vec2 =
        var a: vec3 = fract(p.xyx * vec3(123.34f, 234.34f, 345.65f))
        a = a + dot(a, a + 34.45f)
        fract(vec2(a.x * a.y, a.y * a.z))

      def fragment(color: vec4): vec4 =
        val uv: vec2 = (2.0f * env.SCREEN_COORDS - env.SIZE) / env.SIZE.y

        var m: Float       = 0.0f
        val t: Float       = env.TIME
        var minDist: Float = 100.0f

        _for(0.0f, _ < 50.0f, _ + 1.0f) { i =>
          val n: vec2 = N22(vec2(i))
          val p: vec2 = sin(n * t)

          val d = length(uv - p)
          m = m + smoothstep(0.02f, 0.01f, d)

          if d < minDist then minDist = d
        }

        // val col: vec3 = vec3(m) // circles
        // val col: vec3 = vec3(minDist) // simple voronoi
        val col: vec3 = vec3(minDist) + vec3(m) // simple voronoi + circle

        vec4(col, 1.0f)

    }

  val voronoi: UltravioletShader =
    // Ported from: https://www.youtube.com/watch?v=l-07BXzNdPw&feature=youtu.be
    UltravioletShader.entityFragment(
      voronoiId,
      EntityShader.fragment(modifyColor, FragmentEnv.reference)
    )
