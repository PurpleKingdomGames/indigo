package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.shader.ShaderId
import indigo.shared.shader.UniformBlock

final case class ShaderData(
    shaderId: ShaderId,
    uniformBlocks: Batch[UniformBlock],
    channel0: Option[AssetName],
    channel1: Option[AssetName],
    channel2: Option[AssetName],
    channel3: Option[AssetName]
) extends Material
    derives CanEqual:

  def withShaderId(newShaderId: ShaderId): ShaderData =
    this.copy(shaderId = newShaderId)

  def withUniformBlocks(newUniformBlocks: Batch[UniformBlock]): ShaderData =
    this.copy(uniformBlocks = newUniformBlocks)
  def withUniformBlocks(newUniformBlocks: UniformBlock*): ShaderData =
    withUniformBlocks(Batch.fromSeq(newUniformBlocks))
  def addUniformBlock(additional: Batch[UniformBlock]): ShaderData =
    this.copy(uniformBlocks = uniformBlocks ++ additional)
  def addUniformBlock(additional: UniformBlock*): ShaderData =
    addUniformBlock(Batch.fromSeq(additional))

  def withChannel0(assetName: AssetName): ShaderData =
    this.copy(channel0 = Some(assetName))
  def withChannel1(assetName: AssetName): ShaderData =
    this.copy(channel1 = Some(assetName))
  def withChannel2(assetName: AssetName): ShaderData =
    this.copy(channel2 = Some(assetName))
  def withChannel3(assetName: AssetName): ShaderData =
    this.copy(channel3 = Some(assetName))

  lazy val toShaderData: ShaderData =
    this

object ShaderData:

  def apply(shaderId: ShaderId): ShaderData =
    ShaderData(shaderId, Batch.empty, None, None, None, None)

  def apply(shaderId: ShaderId, uniformBlocks: UniformBlock*): ShaderData =
    ShaderData(shaderId, Batch.fromSeq(uniformBlocks), None, None, None, None)

  def apply(
      shaderId: ShaderId,
      channel0: AssetName,
      channel1: AssetName,
      channel2: AssetName,
      channel3: AssetName
  ): ShaderData =
    ShaderData(shaderId, Batch.empty, Option(channel0), Option(channel1), Option(channel2), Option(channel3))
