package indigo.shared.shader

import indigo.shared.assets.AssetName

sealed trait CustomShader {
  def id: ShaderId
}

sealed trait ShaderSource extends CustomShader

object CustomShader {

  val defaultVertexProgram: String       = "void vertex(){}"
  val defaultPostVertexProgram: String   = "void postVertex(){}"
  val defaultFragmentProgram: String     = "void fragment(){}"
  val defaultPostFragmentProgram: String = "void postFragment(){}"
  val defaultLightProgram: String        = "void light(){}"
  val defaultPostLightProgram: String    = "void postLight(){}"

  final case class Source(
      id: ShaderId,
      vertex: String,
      postVertex: String,
      fragment: String,
      postFragment: String,
      light: String,
      postLight: String
  ) extends ShaderSource {
    def withShaderId(newId: ShaderId): Source =
      this.copy(id = newId)

    def withVertexProgram(program: String): Source =
      this.copy(vertex = program)

    def withPostVertexProgram(program: String): Source =
      this.copy(postVertex = program)

    def withFragmentProgram(program: String): Source =
      this.copy(fragment = program)

    def withPostFragmentProgram(program: String): Source =
      this.copy(postFragment = program)

    def withLightProgram(program: String): Source =
      this.copy(light = program)

    def withPostLightProgram(program: String): Source =
      this.copy(postLight = program)
  }
  object Source {

    def apply(id: ShaderId): Source =
      Source(
        id,
        defaultVertexProgram,
        defaultPostVertexProgram,
        defaultFragmentProgram,
        defaultPostFragmentProgram,
        defaultLightProgram,
        defaultPostLightProgram
      )

  }

  final case class External(
      id: ShaderId,
      vertex: Option[AssetName],
      postVertex: Option[AssetName],
      fragment: Option[AssetName],
      postFragment: Option[AssetName],
      light: Option[AssetName],
      postLight: Option[AssetName]
  ) extends ShaderSource {

    def withShaderId(newId: ShaderId): External =
      this.copy(id = newId)

    def withVertexProgram(program: AssetName): External =
      this.copy(vertex = Option(program))

    def withPostVertexProgram(program: AssetName): External =
      this.copy(postVertex = Option(program))

    def withFragmentProgram(program: AssetName): External =
      this.copy(fragment = Option(program))

    def withPostFragmentProgram(program: AssetName): External =
      this.copy(postFragment = Option(program))

    def withLightProgram(program: AssetName): External =
      this.copy(light = Option(program))

    def withPostLightProgram(program: AssetName): External =
      this.copy(postLight = Option(program))

  }
  object External {

    def apply(id: ShaderId): External =
      External(
        id,
        None,
        None,
        None,
        None,
        None,
        None
      )

  }

  final case class PostSource(
      id: ShaderId,
      parentShader: ShaderSource,
      postVertex: String,
      postFragment: String,
      postLight: String
  ) extends CustomShader {
    def withShaderId(newId: ShaderId): PostSource =
      this.copy(id = newId)

    def withParentShader(newParentShader: ShaderSource): PostSource =
      this.copy(parentShader = newParentShader)

    def withPostVertexProgram(program: String): PostSource =
      this.copy(postVertex = program)

    def withPostFragmentProgram(program: String): PostSource =
      this.copy(postFragment = program)

    def withPostLightProgram(program: String): PostSource =
      this.copy(postLight = program)
  }
  object PostSource {

    def apply(id: ShaderId, parentShader: ShaderSource): PostSource =
      PostSource(
        id,
        parentShader,
        defaultPostVertexProgram,
        defaultPostFragmentProgram,
        defaultPostLightProgram
      )

  }

  final case class PostExternal(
      id: ShaderId,
      parentShader: ShaderSource,
      postVertex: Option[AssetName],
      postFragment: Option[AssetName],
      postLight: Option[AssetName]
  ) extends CustomShader {

    def withShaderId(newId: ShaderId): PostExternal =
      this.copy(id = newId)

    def withParentShader(newParentShader: ShaderSource): PostExternal =
      this.copy(parentShader = newParentShader)

    def withPostVertexProgram(program: AssetName): PostExternal =
      this.copy(postVertex = Option(program))

    def withPostFragmentProgram(program: AssetName): PostExternal =
      this.copy(postFragment = Option(program))

    def withPostLightProgram(program: AssetName): PostExternal =
      this.copy(postLight = Option(program))

  }
  object PostExternal {

    def apply(id: ShaderId, parentShader: ShaderSource): PostExternal =
      PostExternal(
        id,
        parentShader,
        None,
        None,
        None
      )

  }

}

final case class Uniform(name: String) extends AnyVal
