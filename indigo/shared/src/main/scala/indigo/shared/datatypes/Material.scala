package indigo.shared.datatypes

import indigo.shared.assets.AssetName

sealed trait Material {
  val isLit: Boolean
  def lit: Material
  def unlit: Material
}

object Material {

  final class Textured(val diffuse: AssetName, val isLit: Boolean) extends Material {
    def withAlbedo(newDiffuse: AssetName): Textured =
      new Textured(newDiffuse, isLit)

    def lit: Textured =
      new Textured(diffuse, true)

    def unlit: Textured =
      new Textured(diffuse, false)
  }
  object Textured {
    def apply(diffuse: AssetName): Textured =
      new Textured(diffuse, false)

    def unapply(t: Textured): Option[AssetName] =
      Some(t.diffuse)
  }

  final class Lit(
      val albedo: AssetName,
      val emission: Option[AssetName],
      val normal: Option[AssetName],
      val specular: Option[AssetName],
      val isLit: Boolean
  ) extends Material {

    def withAlbedo(newAlbedo: AssetName): Lit =
      new Lit(newAlbedo, emission, normal, specular, isLit)

    def withEmission(newEmission: AssetName): Lit =
      new Lit(albedo, Some(newEmission), normal, specular, isLit)

    def withNormal(newNormal: AssetName): Lit =
      new Lit(albedo, emission, Some(newNormal), specular, isLit)

    def withSpecular(newSpecular: AssetName): Lit =
      new Lit(albedo, emission, normal, Some(newSpecular), isLit)

    def lit: Lit =
      new Lit(albedo, emission, normal, specular, true)

    def unlit: Lit =
      new Lit(albedo, emission, normal, specular, false)
  }
  object Lit {
    def apply(
        albedo: AssetName,
        emission: Option[AssetName],
        normal: Option[AssetName],
        specular: Option[AssetName]
    ): Lit =
      new Lit(albedo, emission, normal, specular, true)

    def unapply(l: Lit): Option[(AssetName, Option[AssetName], Option[AssetName], Option[AssetName])] =
      Some((l.albedo, l.emission, l.normal, l.specular))
  }

}
