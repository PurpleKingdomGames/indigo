package indigo.shared.materials

import indigo.shared.assets.AssetName
import indigo.shared.shader.UniformBlock
import indigo.shared.shader.Uniform
import indigo.shared.shader.ShaderPrimitive.vec2
import indigo.shared.shader.ShaderId

sealed trait LightingModel

object LightingModel {

  case object Unlit extends LightingModel

  final case class Lit(
      emissive: Option[Texture],
      normal: Option[Texture],
      roughness: Option[Texture]
  ) extends LightingModel {

    def withEmissive(emissiveAssetName: AssetName, amount: Double): Lit =
      this.copy(emissive = Some(Texture(emissiveAssetName, amount)))
    def withEmissiveAsset(emissiveAssetName: AssetName): Lit =
      this.copy(emissive =
        emissive
          .map(_.withAsset(emissiveAssetName))
          .orElse(Some(Texture(emissiveAssetName, 1.0)))
      )
    def withEmissiveAmount(amount: Double): Lit =
      this.copy(emissive = emissive.map(_.withAmount(amount)))

    def withNormal(normalAssetName: AssetName, amount: Double): Lit =
      this.copy(normal = Some(Texture(normalAssetName, amount)))
    def withNormalAsset(normalAssetName: AssetName): Lit =
      this.copy(normal =
        normal
          .map(_.withAsset(normalAssetName))
          .orElse(Some(Texture(normalAssetName, 1.0)))
      )
    def withNormalAmount(amount: Double): Lit =
      this.copy(normal = normal.map(_.withAmount(amount)))

    def withRoughness(roughnessAssetName: AssetName, amount: Double): Lit =
      this.copy(roughness = Some(Texture(roughnessAssetName, amount)))
    def withRoughnessAsset(roughnessAssetName: AssetName): Lit =
      this.copy(roughness =
        roughness
          .map(_.withAsset(roughnessAssetName))
          .orElse(Some(Texture(roughnessAssetName, 1.0)))
      )
    def withRoughnessAmount(amount: Double): Lit =
      this.copy(roughness = roughness.map(_.withAmount(amount)))

    def toShaderData(shaderId: ShaderId): ShaderData =
      ShaderData(
        shaderId,
        List(
          UniformBlock(
            "IndigoMaterialLightingData",
            List(
              Uniform("LIGHT_EMISSIVE") -> vec2(
                emissive.map(_ => 1.0).getOrElse(-1.0),
                emissive.map(_.amount).getOrElse(0.0)
              ),
              Uniform("LIGHT_NORMAL") -> vec2(
                normal.map(_ => 1.0).getOrElse(-1.0),
                normal.map(_.amount).getOrElse(0.0)
              ),
              Uniform("LIGHT_ROUGHNESS") -> vec2(
                roughness.map(_ => 1.0).getOrElse(-1.0),
                roughness.map(_.amount).getOrElse(0.0)
              )
            )
          )
        ),
        None,
        emissive.map(_.assetName),
        normal.map(_.assetName),
        roughness.map(_.assetName)
      )
  }
  object Lit {
    def apply(): Lit =
      new Lit(None, None, None)

    val flat: Lit =
      Lit()

    def apply(
        emissive: Option[Texture],
        normal: Option[Texture],
        roughness: Option[Texture]
    ): Lit =
      new Lit(emissive, normal, roughness)

    def apply(
        emissive: AssetName
    ): Lit =
      new Lit(
        Some(Texture(emissive, 1.0d)),
        None,
        None
      )

    def apply(
        emissive: AssetName,
        normal: AssetName
    ): Lit =
      new Lit(
        Some(Texture(emissive, 1.0d)),
        Some(Texture(normal, 1.0d)),
        None
      )

    def apply(
        emissive: AssetName,
        normal: AssetName,
        roughness: AssetName
    ): Lit =
      new Lit(
        Some(Texture(emissive, 1.0d)),
        Some(Texture(normal, 1.0d)),
        Some(Texture(roughness, 1.0d))
      )
  }
}

final case class Texture(assetName: AssetName, amount: Double) {
  def withAsset(newAssetName: AssetName): Texture =
    this.copy(assetName = newAssetName)

  def withAmount(newAmount: Double): Texture =
    this.copy(amount = newAmount)
}
