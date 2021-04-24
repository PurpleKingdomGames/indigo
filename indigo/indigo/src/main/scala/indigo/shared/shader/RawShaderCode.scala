package indigo.shared.shader

import indigo.shaders.WebGL2Base
import indigo.shaders.WebGL2Merge

trait RawShaderCode {
  val id: ShaderId
  val vertex: String
  val fragment: String
}

object RawShaderCode {

  def fromEntityShader(customShader: EntityShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId   = customShader.id
      val vertex: String = ShaderTemplates.webGL2EntityVertex(Option(customShader.vertex))
      val fragment: String = ShaderTemplates.webGL2EntityFragment(
        Option(customShader.fragment),
        Option(customShader.prepare),
        Option(customShader.light),
        Option(customShader.composite)
      )
    }

  def fromBlendShader(customShader: BlendShader.Source): RawShaderCode =
    new RawShaderCode {
      val id: ShaderId     = customShader.id
      val vertex: String   = ShaderTemplates.webGL2BlendVertex(Some(customShader.vertex))
      val fragment: String = ShaderTemplates.webGL2BlendFragment(Some(customShader.fragment))
    }

  object ShaderTemplates {

    def webGL2EntityVertex(vertexShader: Option[String]): String =
      WebGL2Base.vertexShader(vertexShader)

    def webGL2EntityFragment(
        fragmentShader: Option[String],
        prepareShader: Option[String],
        lightShader: Option[String],
        compositeShader: Option[String]
    ): String =
      WebGL2Base.fragmentShader(fragmentShader, prepareShader, lightShader, compositeShader)

    def webGL2BlendVertex(vertexShader: Option[String]): String =
      WebGL2Merge.vertexShader(vertexShader)

    def webGL2BlendFragment(
        fragmentShader: Option[String]
    ): String =
      WebGL2Merge.fragmentShader(fragmentShader, None, None, None)

  }

}
