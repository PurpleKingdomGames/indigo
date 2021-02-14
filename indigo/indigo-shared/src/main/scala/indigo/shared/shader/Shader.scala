package indigo.shared.shader

import indigo.shaders.WebGL2Base

trait Shader {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object Shader {

  def fromCustomShader(customShader: CustomShader.Source): Shader =
    new Shader {
      val id: ShaderId = customShader.id
      val vertex: String = ShaderTemplates.webGL2Vertex(
        Some(customShader.vertex),
        Some(customShader.postVertex)
      )
      val fragment: String = ShaderTemplates.webGL2Fragment(
        Some(customShader.fragment),
        Some(customShader.postFragment),
        Some(customShader.light),
        Some(customShader.postLight)
      )
    }

  object ShaderTemplates {

    def webGL2Vertex(vertexShader: Option[String], postVertexShader: Option[String]): String =
      WebGL2Base.vertexShader(vertexShader, postVertexShader)

    def webGL2Fragment(
        fragmentShader: Option[String],
        postFragmentShader: Option[String],
        lightShader: Option[String],
        postLightShader: Option[String]
    ): String =
      WebGL2Base.fragmentShader(fragmentShader, postFragmentShader, lightShader, postLightShader)

  }

}
