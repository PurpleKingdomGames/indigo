package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.shader.ShaderId
import indigo.shared.shader.UniformBlock

final case class ShaderData(
    shaderId: ShaderId,
    uniformBlock: Option[UniformBlock],
    channel0: Option[AssetName],
    channel1: Option[AssetName],
    channel2: Option[AssetName],
    channel3: Option[AssetName]
) extends Material {

  def withShaderId(newShaderId: ShaderId): ShaderData =
    this.copy(shaderId = newShaderId)

  def withUniformBlock(newUniformBlock: UniformBlock): ShaderData =
    this.copy(uniformBlock = Option(newUniformBlock))

  def withChannel0(assetName: AssetName): ShaderData =
    this.copy(channel0 = Some(assetName))
  def withChannel1(assetName: AssetName): ShaderData =
    this.copy(channel1 = Some(assetName))
  def withChannel2(assetName: AssetName): ShaderData =
    this.copy(channel2 = Some(assetName))
  def withChannel3(assetName: AssetName): ShaderData =
    this.copy(channel3 = Some(assetName))

  lazy val hash: String =
    s"custom-${shaderId.value}" +
      s"-${uniformBlock.map(_.uniformHash).getOrElse("")}" +
      s"-${channel0.map(_.value).getOrElse("")}" +
      s"-${channel1.map(_.value).getOrElse("")}" +
      s"-${channel2.map(_.value).getOrElse("")}" +
      s"-${channel3.map(_.value).getOrElse("")}"

  def toShaderData: ShaderData =
    this

}
object ShaderData {

  def apply(shaderId: ShaderId): ShaderData =
    ShaderData(shaderId, None, None, None, None, None)

  def apply(shaderId: ShaderId, uniformBlock: UniformBlock): ShaderData =
    ShaderData(shaderId, Option(uniformBlock), None, None, None, None)

  def apply(shaderId: ShaderId, channel0: AssetName, channel1: AssetName, channel2: AssetName, channel3: AssetName): ShaderData =
    ShaderData(shaderId, None, Option(channel0), Option(channel1), Option(channel2), Option(channel3))

}
