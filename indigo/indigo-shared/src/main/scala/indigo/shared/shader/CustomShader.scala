package indigo.shared.shader

import indigo.shared.assets.AssetName

sealed trait CustomShader {
  def id: ShaderId
}
object CustomShader {

  val defaultVertexProgram: String   = "void vertex(){}"
  val defaultFragmentProgram: String = "void fragment(){}"
  val defaultLightProgram: String    = "void light(){}"

  final case class Source(
      id: ShaderId,
      vertex: String,
      fragment: String,
      light: String
  ) extends CustomShader {
    def withShaderId(newId: ShaderId): Source =
      this.copy(id = newId)

    def withVertexProgram(program: String): Source =
      this.copy(vertex = program)

    def withFragmentProgram(program: String): Source =
      this.copy(fragment = program)

    def withLightProgram(program: String): Source =
      this.copy(light = program)
  }
  object Source {

    def apply(id: ShaderId): Source =
      Source(
        id,
        defaultVertexProgram,
        defaultFragmentProgram,
        defaultLightProgram
      )

  }

  final case class External(
      id: ShaderId,
      vertex: Option[AssetName],
      fragment: Option[AssetName],
      light: Option[AssetName]
  ) extends CustomShader {

    def withShaderId(newId: ShaderId): External =
      this.copy(id = newId)

    def withVertexProgram(program: AssetName): External =
      this.copy(vertex = Option(program))

    def withFragmentProgram(program: AssetName): External =
      this.copy(fragment = Option(program))

    def withLightProgram(program: AssetName): External =
      this.copy(light = Option(program))

  }
  object External {

    def apply(id: ShaderId): External =
      External(
        id,
        None,
        None,
        None
      )

  }

}

final case class Uniform(name: String) extends AnyVal
