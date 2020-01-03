package indigo.shared.datatypes

import indigo.shared.assets.AssetName

sealed trait Material {
  val name: BindingKey
  val color: Tint
  def withName(newName: BindingKey): Material
  def withColor(newColor: Tint): Material
}

object Material {

  final class Colored(val name: BindingKey, val color: Tint) extends Material {
    def withName(newName: BindingKey): Colored =
      new Colored(newName, color)

    def withColor(newColor: Tint): Colored =
      new Colored(name, newColor)
  }

  final class Textured(val name: BindingKey, val color: Tint, val diffuse: MaterialChannel) extends Material {
    def withName(newName: BindingKey): Textured =
      new Textured(newName, color, diffuse)

    def withColor(newColor: Tint): Textured =
      new Textured(name, newColor, diffuse)
  }

  final class Lit(
      val name: BindingKey,
      val color: Tint,
      val albedo: Option[MaterialChannel],
      val emission: Option[MaterialChannel],
      val normal: Option[MaterialChannel],
      val specular: Option[MaterialChannel]
  ) extends Material {
    def withName(newName: BindingKey): Lit =
      new Lit(newName, color, albedo, emission, normal, specular)

    def withColor(newColor: Tint): Lit =
      new Lit(name, newColor, albedo, emission, normal, specular)

    def withAlbedo(newAlbedo: MaterialChannel): Lit =
      new Lit(name, color, Some(newAlbedo), emission, normal, specular)

    def withEmission(newEmission: MaterialChannel): Lit =
      new Lit(name, color, albedo, Some(newEmission), normal, specular)

    def withNormal(newNormal: MaterialChannel): Lit =
      new Lit(name, color, albedo, emission, Some(newNormal), specular)

    def withSpecular(newSpecular: MaterialChannel): Lit =
      new Lit(name, color, albedo, emission, normal, Some(newSpecular))
  }

}

final class MaterialChannel(val assetName: AssetName, val amount: Double) {
  def withAssetName(newAssetName: AssetName): MaterialChannel =
    new MaterialChannel(newAssetName, amount)

  def withAmount(newAmount: Double): MaterialChannel =
    new MaterialChannel(assetName, Math.min(1, Math.max(0, newAmount)))
}
object MaterialChannel {
  def apply(assetName: AssetName, amount: Double): MaterialChannel =
    new MaterialChannel(assetName, Math.min(1, Math.max(0, amount)))
}
