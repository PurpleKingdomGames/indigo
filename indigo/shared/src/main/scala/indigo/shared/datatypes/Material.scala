package indigo.shared.datatypes

import indigo.shared.assets.AssetName
import indigo.shared.EqualTo
import indigo.shared.EqualTo._

sealed trait Material {
  val default: AssetName
  val isLit: Boolean
  def lit: Material
  def unlit: Material
  def hash: String
  def isGreen: Boolean
  def drawGreen: Material
}

object Material {

  implicit val eq: EqualTo[Material] =
    EqualTo.create {
      case (Textured(diffuseA, isLitA, isGreenA), Textured(diffuseB, isLitB, isGreenB)) =>
        diffuseA === diffuseB &&
          isLitA === isLitB &&
          isGreenA === isGreenB

      case (Lit(albedoA, emissiveA, normalA, specularA, isLitA, isGreenA), Lit(albedoB, emissiveB, normalB, specularB, isLitB, isGreenB)) =>
        albedoA === albedoB &&
          emissiveA === emissiveB &&
          normalA === normalB &&
          specularA === specularB &&
          isLitA === isLitB &&
          isGreenA === isGreenB
      case _ =>
        false
    }

  final case class Textured(diffuse: AssetName, isLit: Boolean, isGreen: Boolean) extends Material {
    val default: AssetName = diffuse

    def withDiffuse(newDiffuse: AssetName): Textured =
      this.copy(diffuse = newDiffuse)

    def lit: Textured =
      this.copy(isLit = true)

    def unlit: Textured =
      this.copy(isLit = false)

    def drawGreen: Textured =
      this.copy(isGreen = true)

    lazy val hash: String =
      diffuse.value + (if (isLit) "1" else "0")
  }
  object Textured {
    def apply(diffuse: AssetName): Textured =
      new Textured(diffuse, false, false)

    def unapply(t: Textured): Option[(AssetName, Boolean)] =
      Some((t.diffuse, t.isLit))
  }

  final case class Lit(
      albedo: AssetName,
      emissive: Option[Texture],
      normal: Option[Texture],
      specular: Option[Texture],
      isLit: Boolean,
      isGreen: Boolean
  ) extends Material {
    val default: AssetName = albedo

    def withAlbedo(newAlbedo: AssetName): Lit =
      this.copy(albedo = newAlbedo)

    def withEmission(emissiveAssetName: AssetName, amount: Double): Lit =
      this.copy(emissive = Some(Texture(emissiveAssetName, amount)))

    def withNormal(normalAssetName: AssetName, amount: Double): Lit =
      this.copy(normal = Some(Texture(normalAssetName, amount)))

    def withSpecular(specularAssetName: AssetName, amount: Double): Lit =
      this.copy(specular = Some(Texture(specularAssetName, amount)))

    def lit: Lit =
      this.copy(isLit = true)

    def unlit: Lit =
      this.copy(isLit = false)

    def drawGreen: Lit =
      this.copy(isGreen = true)

    lazy val hash: String =
      albedo.value +
        emissive.map(_.hash).getOrElse("_") +
        normal.map(_.hash).getOrElse("_") +
        specular.map(_.hash).getOrElse("_") +
        (if (isLit) "1" else "0")
  }
  object Lit {
    def apply(
        albedo: AssetName,
        emissive: Option[Texture],
        normal: Option[Texture],
        specular: Option[Texture]
    ): Lit =
      new Lit(albedo, emissive, normal, specular, true, false)

    def apply(
        albedo: AssetName
    ): Lit =
      new Lit(albedo, None, None, None, true, false)

    def apply(
        albedo: AssetName,
        emissive: AssetName
    ): Lit =
      new Lit(
        albedo,
        Some(Texture(emissive, 1.0d)),
        None,
        None,
        true,
        false
      )

    def apply(
        albedo: AssetName,
        emissive: AssetName,
        normal: AssetName
    ): Lit =
      new Lit(
        albedo,
        Some(Texture(emissive, 1.0d)),
        Some(Texture(normal, 1.0d)),
        None,
        true,
        false
      )

    def apply(
        albedo: AssetName,
        emissive: AssetName,
        normal: AssetName,
        specular: AssetName
    ): Lit =
      new Lit(
        albedo,
        Some(Texture(emissive, 1.0d)),
        Some(Texture(normal, 1.0d)),
        Some(Texture(specular, 1.0d)),
        true,
        false
      )

    def fromAlbedo(albedo: AssetName): Lit =
      new Lit(albedo, None, None, None, true, false)
  }

}

final case class Texture(assetName: AssetName, amount: Double) {
  def hash: String =
    assetName.value + amount.toString()
}
object Texture {

  implicit val eq: EqualTo[Texture] =
    EqualTo.create {
      case (a, b) =>
        a.assetName === b.assetName &&
          a.amount === b.amount
    }

}
