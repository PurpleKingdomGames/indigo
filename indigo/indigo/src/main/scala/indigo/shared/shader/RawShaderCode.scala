package indigo.shared.shader

import ultraviolet.datatypes.ShaderResult

trait RawShaderCode {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object RawShaderCode {

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  def fromUltravioletShader(customShader: UltravioletShader): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId = customShader.id
      val vertex: String = customShader.vertex match
        case ShaderResult.Error(reason) =>
          throw new Exception("Invalid Ultraviolet vertex shader: " + reason)

        case ShaderResult.Output(code, metadata) =>
          code

      val fragment: String = customShader.fragment match
        case ShaderResult.Error(reason) =>
          throw new Exception("Invalid Ultraviolet fragment shader: " + reason)

        case ShaderResult.Output(code, metadata) =>
          code
    }

  def fromEntityShader(customShader: EntityShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId =
        customShader.id

      val vertex: String =
        EntityShader.vertexTemplate(customShader.vertex)

      val fragment: String =
        EntityShader.fragmentTemplate(
          s"""
          |${customShader.fragment}
          |
          |${customShader.prepare}
          |
          |${customShader.light}
          |
          |${customShader.composite}
          |""".stripMargin.trim
        )
    }

  def fromBlendShader(customShader: BlendShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId =
        customShader.id

      val vertex: String =
        BlendShader.vertexTemplate(customShader.vertex)

      val fragment: String =
        BlendShader.fragmentTemplate(customShader.fragment)
    }

}
