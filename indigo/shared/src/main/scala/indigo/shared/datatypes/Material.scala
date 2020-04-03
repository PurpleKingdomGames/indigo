package indigo.shared.datatypes

import indigo.shared.assets.AssetName

sealed trait Material {
  val default: AssetName
  val isLit: Boolean
  def lit: Material
  def unlit: Material
  def hash: String
}

object Material {

  final class Textured(val diffuse: AssetName, val isLit: Boolean) extends Material {
    val default: AssetName = diffuse

    def withDiffuse(newDiffuse: AssetName): Textured =
      new Textured(newDiffuse, isLit)

    def lit: Textured =
      new Textured(diffuse, true)

    def unlit: Textured =
      new Textured(diffuse, false)

    lazy val hash: String =
      diffuse.value + (if (isLit) "1" else "0")
  }
  object Textured {
    def apply(diffuse: AssetName): Textured =
      new Textured(diffuse, false)

    def unapply(t: Textured): Option[AssetName] =
      Some(t.diffuse)
  }

  final class Lit(
      val albedo: AssetName,
      val emission: Option[Texture],
      val normal: Option[Texture],
      val specular: Option[Texture],
      val isLit: Boolean
  ) extends Material {
    val default: AssetName = albedo

    def withAlbedo(newAlbedo: AssetName): Lit =
      new Lit(newAlbedo, emission, normal, specular, isLit)

    def withEmission(emissionAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, Some(Texture(emissionAssetName, amount)), normal, specular, isLit)

    def withNormal(normalAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, emission, Some(Texture(normalAssetName, amount)), specular, isLit)

    def withSpecular(specularAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, emission, normal, Some(Texture(specularAssetName, amount)), isLit)

    def lit: Lit =
      new Lit(albedo, emission, normal, specular, true)

    def unlit: Lit =
      new Lit(albedo, emission, normal, specular, false)

    lazy val hash: String =
      albedo.value +
        emission.map(_.hash).getOrElse("_") +
        normal.map(_.hash).getOrElse("_") +
        specular.map(_.hash).getOrElse("_") +
        (if (isLit) "1" else "0")
  }
  object Lit {
    def apply(
        albedo: AssetName,
        emission: Option[Texture],
        normal: Option[Texture],
        specular: Option[Texture]
    ): Lit =
      new Lit(albedo, emission, normal, specular, true)

    def unapply(l: Lit): Option[(AssetName, Option[Texture], Option[Texture], Option[Texture])] =
      Some((l.albedo, l.emission, l.normal, l.specular))

    def fromAlbedo(albedo: AssetName): Lit =
      new Lit(albedo, None, None, None, true)
  }

}

final class Texture(val assetName: AssetName, val amount: Double) {
  def hash: String =
    assetName.value + amount.toString()
}
object Texture {
  def apply(assetName: AssetName, amount: Double): Texture =
    new Texture(assetName, amount)

  def unapply(t: Texture): Option[(AssetName, Double)] =
    Some((t.assetName, t.amount))
}
