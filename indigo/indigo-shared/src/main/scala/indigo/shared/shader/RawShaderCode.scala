package indigo.shared.shader

import indigo.shaders.WebGL2Base

trait RawShaderCode {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object RawShaderCode {

  def fromCustomShader(customShader: Shader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId = customShader.id
      val vertex: String = ShaderTemplates.webGL2Vertex(
        Some(customShader.vertex)
      )
      val fragment: String = ShaderTemplates.webGL2Fragment(
        Some(customShader.fragment),
        Some(customShader.light)
      )
    }

  object ShaderTemplates {

    def webGL2Vertex(vertexShader: Option[String]): String =
      WebGL2Base.vertexShader(vertexShader)

    def webGL2Fragment(
        fragmentShader: Option[String],
        lightShader: Option[String]
    ): String =
      WebGL2Base.fragmentShader(fragmentShader, lightShader)

  }

}
