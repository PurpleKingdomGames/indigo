package indigo.shared.materials

import indigo.shared.collections.Batch
import indigo.shared.shader.ShaderId
import indigo.shared.shader.UniformBlock

final case class BlendShaderData(
    shaderId: ShaderId,
    uniformBlocks: Batch[UniformBlock]
) extends BlendMaterial
    derives CanEqual:

  def withShaderId(newShaderId: ShaderId): BlendShaderData =
    this.copy(shaderId = newShaderId)

  def withUniformBlock(newUniformBlocks: Batch[UniformBlock]): BlendShaderData =
    this.copy(uniformBlocks = newUniformBlocks)
  def withUniformBlock(newUniformBlocks: UniformBlock*): BlendShaderData =
    withUniformBlock(Batch.fromSeq(newUniformBlocks))

  lazy val toShaderData: BlendShaderData =
    this

object BlendShaderData:

  def apply(shaderId: ShaderId): BlendShaderData =
    BlendShaderData(shaderId, Batch.empty)

  def apply(shaderId: ShaderId, uniformBlocks: UniformBlock*): BlendShaderData =
    BlendShaderData(shaderId, Batch.fromSeq(uniformBlocks))
