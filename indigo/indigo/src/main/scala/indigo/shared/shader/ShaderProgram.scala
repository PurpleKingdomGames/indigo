package indigo.shared.shader

import indigo.shared.assets.AssetName
import indigo.shared.shader.library.BaseBlendShader
import indigo.shared.shader.library.BaseEntityShader
import indigo.shared.shader.library.IndigoUV
import ultraviolet.datatypes.ShaderResult
import ultraviolet.syntax.Shader as UVShader
import ultraviolet.syntax.vec4

import scala.annotation.nowarn

sealed trait ShaderProgram derives CanEqual:
  def id: ShaderId

object ShaderProgram:
  val defaultVertexProgram: String =
    """vec4 vertex(vec4 v){
    |  return v;
    |}
    |""".stripMargin.trim
  val defaultFragmentProgram: String =
    """vec4 fragment(vec4 v){
    |  return v;
    |}
    |""".stripMargin.trim
  val defaultPrepareProgram: String =
    """void prepare(){}"""
  val defaultLightProgram: String =
    """void light(){}"""
  val defaultCompositeProgram: String =
    """void composite(){}"""

final case class UltravioletShader(id: ShaderId, vertex: ShaderResult, fragment: ShaderResult) extends ShaderProgram
object UltravioletShader:

  @nowarn("msg=unused")
  inline def noopVertex: UVShader[IndigoUV.VertexEnv, Unit] =
    UVShader[IndigoUV.VertexEnv] { _ =>
      def vertex(v: vec4): vec4 =
        v
    }

  inline def entityFragment(id: ShaderId, fragment: ShaderResult): UltravioletShader =
    UltravioletShader(
      id,
      EntityShader.vertex(noopVertex, IndigoUV.VertexEnv.reference),
      fragment
    )

  inline def blendFragment(id: ShaderId, fragment: ShaderResult): UltravioletShader =
    UltravioletShader(
      id,
      BlendShader.vertex(noopVertex, IndigoUV.VertexEnv.reference),
      fragment
    )

sealed trait EntityShader extends ShaderProgram
object EntityShader extends BaseEntityShader:

  final case class Source(
      id: ShaderId,
      vertex: String,
      fragment: String,
      prepare: String,
      light: String,
      composite: String
  ) extends EntityShader:
    def withShaderId(newId: ShaderId): Source =
      this.copy(id = newId)

    def withVertexProgram(program: String): Source =
      this.copy(vertex = program)

    def withFragmentProgram(program: String): Source =
      this.copy(fragment = program)

    def withPrepareProgram(program: String): Source =
      this.copy(prepare = program)

    def withLightProgram(program: String): Source =
      this.copy(light = program)

    def withCompositeProgram(program: String): Source =
      this.copy(composite = program)

  object Source:

    def apply(id: ShaderId): Source =
      Source(
        id,
        ShaderProgram.defaultVertexProgram,
        ShaderProgram.defaultFragmentProgram,
        ShaderProgram.defaultPrepareProgram,
        ShaderProgram.defaultLightProgram,
        ShaderProgram.defaultCompositeProgram
      )

  final case class External(
      id: ShaderId,
      vertex: Option[AssetName],
      fragment: Option[AssetName],
      prepare: Option[AssetName],
      light: Option[AssetName],
      composite: Option[AssetName]
  ) extends EntityShader:
    def withShaderId(newId: ShaderId): External =
      this.copy(id = newId)

    def withVertexProgram(program: AssetName): External =
      this.copy(vertex = Option(program))

    def withFragmentProgram(program: AssetName): External =
      this.copy(fragment = Option(program))

    def withPrepareProgram(program: AssetName): External =
      this.copy(prepare = Option(program))

    def withLightProgram(program: AssetName): External =
      this.copy(light = Option(program))

    def withCompositeProgram(program: AssetName): External =
      this.copy(composite = Option(program))

  object External:

    def apply(id: ShaderId): External =
      External(
        id,
        None,
        None,
        None,
        None,
        None
      )

sealed trait BlendShader extends ShaderProgram
object BlendShader extends BaseBlendShader:

  final case class Source(
      id: ShaderId,
      vertex: String,
      fragment: String
  ) extends BlendShader:
    def withShaderId(newId: ShaderId): Source =
      this.copy(id = newId)

    def withVertexProgram(program: String): Source =
      this.copy(vertex = program)

    def withFragmentProgram(program: String): Source =
      this.copy(fragment = program)

  object Source:

    def apply(id: ShaderId): Source =
      Source(
        id,
        ShaderProgram.defaultVertexProgram,
        ShaderProgram.defaultFragmentProgram
      )

  final case class External(
      id: ShaderId,
      vertex: Option[AssetName],
      fragment: Option[AssetName]
  ) extends BlendShader:
    def withShaderId(newId: ShaderId): External =
      this.copy(id = newId)

    def withVertexProgram(program: AssetName): External =
      this.copy(vertex = Option(program))

    def withFragmentProgram(program: AssetName): External =
      this.copy(fragment = Option(program))

  object External:
    def apply(id: ShaderId): External =
      External(
        id,
        None,
        None
      )
