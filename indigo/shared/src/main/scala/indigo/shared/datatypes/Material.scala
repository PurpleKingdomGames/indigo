package indigo.shared.datatypes

import indigo.shared.assets.AssetName

sealed trait Material

object Material {

  final class Textured(val diffuse: AssetName) extends Material {
    def withAlbedo(newDiffuse: AssetName): Textured =
      new Textured(newDiffuse)
  }
  object Textured {
    def apply(diffuse: AssetName): Textured =
      new Textured(diffuse)

    def unapply(t: Textured): Option[AssetName] =
      Some(t.diffuse)
  }

  final class Lit(
      val albedo: AssetName,
      val emission: Option[AssetName],
      val normal: Option[AssetName],
      val specular: Option[AssetName]
  ) extends Material {

    def withAlbedo(newAlbedo: AssetName): Lit =
      new Lit(newAlbedo, emission, normal, specular)

    def withEmission(newEmission: AssetName): Lit =
      new Lit(albedo, Some(newEmission), normal, specular)

    def withNormal(newNormal: AssetName): Lit =
      new Lit(albedo, emission, Some(newNormal), specular)

    def withSpecular(newSpecular: AssetName): Lit =
      new Lit(albedo, emission, normal, Some(newSpecular))
  }
  object Lit {
    def apply(
        albedo: AssetName,
        emission: Option[AssetName],
        normal: Option[AssetName],
        specular: Option[AssetName]
    ): Lit =
      new Lit(albedo, emission, normal, specular)

    def unapply(l: Lit): Option[(AssetName, Option[AssetName], Option[AssetName], Option[AssetName])] =
      Some((l.albedo, l.emission, l.normal, l.specular))
  }

}
