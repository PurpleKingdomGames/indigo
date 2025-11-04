package indigo.shared.shader

import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.shader.ShaderId

final case class ShaderData(
    shaderId: ShaderId,
    uniformBlocks: Batch[UniformBlock],
    channel0: Option[AssetName],
    channel1: Option[AssetName],
    channel2: Option[AssetName],
    channel3: Option[AssetName]
) derives CanEqual:

  def withShaderId(newShaderId: ShaderId): ShaderData =
    this.copy(shaderId = newShaderId)

  def addUniformData[A](ubo: A)(using toUBO: ToUniformBlock[A]): ShaderData =
    this.copy(uniformBlocks = uniformBlocks :+ toUBO.toUniformBlock(ubo))

  def withUniformBlocks(newUniformBlocks: Batch[UniformBlock]): ShaderData =
    this.copy(uniformBlocks = newUniformBlocks)

  def withChannel0(assetName: AssetName): ShaderData =
    this.copy(channel0 = Some(assetName))
  def withChannel1(assetName: AssetName): ShaderData =
    this.copy(channel1 = Some(assetName))
  def withChannel2(assetName: AssetName): ShaderData =
    this.copy(channel2 = Some(assetName))
  def withChannel3(assetName: AssetName): ShaderData =
    this.copy(channel3 = Some(assetName))

object ShaderData:

  def apply(shaderId: ShaderId): ShaderData =
    ShaderData(shaderId, Batch.empty, None, None, None, None)

  def apply[A](shaderId: ShaderId, uniformData: A)(using toUBO: ToUniformBlock[A]): ShaderData =
    ShaderData(shaderId, Batch(toUBO.toUniformBlock(uniformData)), None, None, None, None)

  def apply(shaderId: ShaderId, uniformBlocks: Batch[UniformBlock]): ShaderData =
    ShaderData(shaderId, uniformBlocks, None, None, None, None)

  def apply(
      shaderId: ShaderId,
      channel0: AssetName,
      channel1: AssetName,
      channel2: AssetName,
      channel3: AssetName
  ): ShaderData =
    ShaderData(shaderId, Batch.empty, Option(channel0), Option(channel1), Option(channel2), Option(channel3))
