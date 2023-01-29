package indigo.shared.shader

import indigo.shared.assets.AssetName
import indigo.shared.shader.library.BaseBlendShader
import indigo.shared.shader.library.BaseEntityShader
import ultraviolet.datatypes.ShaderResult

sealed trait Shader derives CanEqual:
  def id: ShaderId

object Shader:
  val defaultVertexProgram: String    = "void vertex(){}"
  val defaultFragmentProgram: String  = "void fragment(){}"
  val defaultPrepareProgram: String   = "void prepare(){}"
  val defaultLightProgram: String     = "void light(){}"
  val defaultCompositeProgram: String = "void composite(){}"

final case class UltravioletShader(id: ShaderId, vertex: ShaderResult, fragment: ShaderResult) extends Shader

sealed trait EntityShader extends Shader
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
        Shader.defaultVertexProgram,
        Shader.defaultFragmentProgram,
        Shader.defaultPrepareProgram,
        Shader.defaultLightProgram,
        Shader.defaultCompositeProgram
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

sealed trait BlendShader extends Shader
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
        Shader.defaultVertexProgram,
        Shader.defaultFragmentProgram
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
