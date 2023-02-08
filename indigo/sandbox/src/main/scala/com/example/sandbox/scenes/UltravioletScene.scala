package com.example.sandbox.scenes

import com.example.sandbox.SandboxAssets
import com.example.sandbox.SandboxGameModel
import com.example.sandbox.SandboxStartupData
import com.example.sandbox.SandboxView
import com.example.sandbox.SandboxViewModel
import indigo.*
import indigo.scenes.*

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
            UVEntity(10, 10, 150, 150, Depth.zero, ShaderData(UVShaders.voronoiId)),
            UVEntity(140, 50, 32, 32, Depth.zero, ShaderData(UVShaders.circleId))
          )
        )
        .withBlendMaterial(MakeRedBlend)
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

case object MakeRedBlend extends BlendMaterial derives CanEqual {
  lazy val toShaderData: BlendShaderData =
    BlendShaderData(
      UVShaders.redBlendId,
      Batch.empty
    )
}

object UVShaders:

  import ultraviolet.syntax.*

  // Blend

  inline def makeRedder: vec4 => Shader[BlendFragmentEnv, vec4] =
    (color: vec4) =>
      Shader { env =>
        val amount = abs(sin(env.TIME))
        vec4(color.rgb * vec3(1.0, amount, amount), color.a)
      }

  val redBlendId: ShaderId =
    ShaderId("red blend")

  val redBlend: UltravioletShader =
    UltravioletShader.blendFragment(
      redBlendId,
      BlendShader.fragment(makeRedder)
    )

  // Circle - Entity

  inline def orbitVertex: Shader[VertexEnv, Unit] =
    Shader[VertexEnv] { env =>
      def vertex(v: vec4): vec4 =
        vec4(v.x + sin(env.TIME * 0.5f), v.y + cos(env.TIME * 0.5f), v.z, v.w)
    }

  inline def circleSdf = (p: vec2, r: Float) => length(p) - r

  inline def calculateColour = (uv: vec2, sdf: Float) =>
    val fill       = vec4(uv, 0.0f, 1.0f)
    val fillAmount = (1.0f - step(0.0f, sdf)) * fill.w
    vec4(fill.xyz * fillAmount, fillAmount)

  inline def modifyCircleColor: vec4 => Shader[FragmentEnv, vec4] =
    _ =>
      Shader[FragmentEnv, vec4] { env =>
        val sdf = circleSdf(env.UV - 0.5f, 0.5f)
        calculateColour(env.UV, sdf)
      }

  val circleId: ShaderId =
    ShaderId("uv circle")

  val circle: UltravioletShader =
    val vert = EntityShader.vertex(orbitVertex)
    println(vert.toOutput.code)

    UltravioletShader(
      circleId,
      vert,
      EntityShader.fragment(modifyCircleColor)
    )

  // Voronoi - Entity

  val voronoiId = ShaderId("uv voronoi")

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def N22 = (p: vec2) =>
    var a: vec3 = fract(p.xyx * vec3(123.34f, 234.34f, 345.65f))
    a = a + dot(a, a + 34.45f)
    fract(vec2(a.x * a.y, a.y * a.z))

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def modifyColor: vec4 => Shader[FragmentEnv, vec4] =
    _ =>
      Shader[FragmentEnv, vec4] { env =>
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
      EntityShader.fragment(modifyColor)
    )
