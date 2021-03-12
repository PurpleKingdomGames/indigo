package indigo.shared.materials

import indigo.shared.shader.ShaderId
import indigo.shared.shader.UniformBlock

final case class BlendShaderData(
    shaderId: ShaderId,
    uniformBlock: Option[UniformBlock]
) extends BlendMaterial {

  def withShaderId(newShaderId: ShaderId): BlendShaderData =
    this.copy(shaderId = newShaderId)

  def withUniformBlock(newUniformBlock: UniformBlock): BlendShaderData =
    this.copy(uniformBlock = Option(newUniformBlock))

  lazy val hash: String =
    s"custom-${shaderId.value}" + s"-${uniformBlock.map(_.uniformHash).getOrElse("")}"

  def toShaderData: BlendShaderData =
    this

}
object BlendShaderData {

  def apply(shaderId: ShaderId): BlendShaderData =
    BlendShaderData(shaderId, None)

  def apply(shaderId: ShaderId, uniformBlock: UniformBlock): BlendShaderData =
    BlendShaderData(shaderId, Option(uniformBlock))

}
