package indigo.shared.shader

import indigo.shared.assets.AssetName

sealed trait CustomShader {
  def id: ShaderId
  def uniforms: Map[Uniform, ShaderPrimitive]
}
object CustomShader {

  val defaultVertexProgram: String   = "void vertex(){}"
  val defaultFragmentProgram: String = "void fragment(){}"
  val defaultLightProgram: String    = "void light(){}"

  final case class Source(
      id: ShaderId,
      vertex: String,
      fragment: String,
      light: String,
      uniforms: Map[Uniform, ShaderPrimitive]
  ) extends CustomShader {
    def withShaderId(newId: ShaderId): Source =
      this.copy(id = newId)

    def withVertexProgram(program: String): Source =
      this.copy(vertex = program)

    def withFragmentProgram(program: String): Source =
      this.copy(fragment = program)

    def withLightProgram(program: String): Source =
      this.copy(light = program)

    def withUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): Source =
      this.copy(uniforms = newUniforms.toMap)
    def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): Source =
      withUniforms(newUniforms.toList)

    def addUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): Source =
      this.copy(uniforms = uniforms ++ newUniforms)
    def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): Source =
      addUniforms(newUniforms.toList)
  }
  object Source {

    def apply(id: ShaderId): Source =
      Source(
        id,
        defaultVertexProgram,
        defaultFragmentProgram,
        defaultLightProgram,
        Map.empty[Uniform, ShaderPrimitive]
      )

  }

  final case class External(
      id: ShaderId,
      vertex: Option[AssetName],
      fragment: Option[AssetName],
      light: Option[AssetName],
      uniforms: Map[Uniform, ShaderPrimitive]
  ) extends CustomShader {

    def withShaderId(newId: ShaderId): External =
      this.copy(id = newId)

    def withVertexProgram(program: AssetName): External =
      this.copy(vertex = Option(program))

    def withFragmentProgram(program: AssetName): External =
      this.copy(fragment = Option(program))

    def withLightProgram(program: AssetName): External =
      this.copy(light = Option(program))

    def withUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): External =
      this.copy(uniforms = newUniforms.toMap)
    def withUniforms(newUniforms: (Uniform, ShaderPrimitive)*): External =
      withUniforms(newUniforms.toList)

    def addUniforms(newUniforms: List[(Uniform, ShaderPrimitive)]): External =
      this.copy(uniforms = uniforms ++ newUniforms)
    def addUniforms(newUniforms: (Uniform, ShaderPrimitive)*): External =
      addUniforms(newUniforms.toList)

  }
  object External {

    def apply(id: ShaderId): External =
      External(
        id,
        None,
        None,
        None,
        Map.empty[Uniform, ShaderPrimitive]
      )

  }

}

final case class Uniform(name: String) extends AnyVal
