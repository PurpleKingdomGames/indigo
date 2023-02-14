import indigo.*

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ShaderGame extends IndigoShader:

  val config: GameConfig =
    GameConfig.default

  val assets: Set[AssetType] =
    SeascapeShader.assets

  val shader: Shader =
    // VoronoiShader.shader
    SeascapeShader.shader

// Seascape by TDM - https://www.shadertoy.com/view/Ms2SD1
object SeascapeShader:

  private val seaAsset: AssetName = AssetName("sea")

  val shader: EntityShader.External =
    EntityShader
      .External(ShaderId("sea"))
      .withFragmentProgram(seaAsset)

  def assets: Set[AssetType] =
    Set(
      AssetType.Text(seaAsset, AssetPath("assets/sea.frag"))
    )

object VoronoiShader:

  val shader: Shader =
    UltravioletShader.entityFragment(
      ShaderId("my shader"),
      EntityShader.fragment[FragmentEnv](voronoi, FragmentEnv.reference)
    )

  import ultraviolet.syntax.*

  // Ported from: https://www.youtube.com/watch?v=l-07BXzNdPw&feature=youtu.be
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  inline def voronoi: Shader[FragmentEnv, Unit] =
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

        val col: vec3 = vec3(minDist) + vec3(m) // simple voronoi + circle

        vec4(col, 1.0f)

    }
