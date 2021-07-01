package indigo.shared.materials

import indigo.shared.shader.ShaderId
import indigo.shared.shader.UniformBlock

final case class BlendShaderData(
    shaderId: ShaderId,
    uniformBlocks: List[UniformBlock]
) extends BlendMaterial derives CanEqual:

  def withShaderId(newShaderId: ShaderId): BlendShaderData =
    this.copy(shaderId = newShaderId)

  def withUniformBlock(newUniformBlocks: List[UniformBlock]): BlendShaderData =
    this.copy(uniformBlocks = newUniformBlocks)
  def withUniformBlock(newUniformBlocks: UniformBlock*): BlendShaderData =
    withUniformBlock(newUniformBlocks.toList)

  def toShaderData: BlendShaderData =
    this

object BlendShaderData:

  def apply(shaderId: ShaderId): BlendShaderData =
    BlendShaderData(shaderId, Nil)

  def apply(shaderId: ShaderId, uniformBlocks: UniformBlock*): BlendShaderData =
    BlendShaderData(shaderId, uniformBlocks.toList)
