package indigo.shared.datatypes

import indigo.shared.assets.AssetName
import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.AsString
import indigo.shared.AsString._

sealed trait Material {
  val default: AssetName
  val isLit: Boolean
  def lit: Material
  def unlit: Material
  def hash: String
}

object Material {

  implicit val show: AsString[Material] =
    AsString.create {
      case t: Textured =>
        s"""Textured(${t.diffuse.toString()}, ${t.isLit.toString()})"""

      case l: Lit =>
        s"""Lit(${l.albedo.toString()}, ${l.emissive.show}, ${l.normal.show}, ${l.specular.show}, ${l.isLit.show})"""
    }

  implicit val eq: EqualTo[Material] =
    EqualTo.create {
      case (Textured(diffuseA, isLitA), Textured(diffuseB, isLitB)) =>
        diffuseA === diffuseB &&
          isLitA === isLitB

      case (Lit(albedoA, emissiveA, normalA, specularA, isLitA), Lit(albedoB, emissiveB, normalB, specularB, isLitB)) =>
        albedoA === albedoB &&
          emissiveA === emissiveB &&
          normalA === normalB &&
          specularA === specularB &&
          isLitA === isLitB
      case _ =>
        false
    }

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

    def unapply(t: Textured): Option[(AssetName, Boolean)] =
      Some((t.diffuse, t.isLit))
  }

  final class Lit(
      val albedo: AssetName,
      val emissive: Option[Texture],
      val normal: Option[Texture],
      val specular: Option[Texture],
      val isLit: Boolean
  ) extends Material {
    val default: AssetName = albedo

    def withAlbedo(newAlbedo: AssetName): Lit =
      new Lit(newAlbedo, emissive, normal, specular, isLit)

    def withEmission(emissiveAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, Some(Texture(emissiveAssetName, amount)), normal, specular, isLit)

    def withNormal(normalAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, emissive, Some(Texture(normalAssetName, amount)), specular, isLit)

    def withSpecular(specularAssetName: AssetName, amount: Double): Lit =
      new Lit(albedo, emissive, normal, Some(Texture(specularAssetName, amount)), isLit)

    def lit: Lit =
      new Lit(albedo, emissive, normal, specular, true)

    def unlit: Lit =
      new Lit(albedo, emissive, normal, specular, false)

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
      new Lit(albedo, emissive, normal, specular, true)

    def apply(
        albedo: AssetName
    ): Lit =
      new Lit(albedo, None, None, None, true)

    def apply(
        albedo: AssetName,
        emissive: AssetName
    ): Lit =
      new Lit(
        albedo,
        Some(Texture(emissive, 1.0d)),
        None,
        None,
        true
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
        true
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
        true
      )

    def unapply(l: Lit): Option[(AssetName, Option[Texture], Option[Texture], Option[Texture], Boolean)] =
      Some((l.albedo, l.emissive, l.normal, l.specular, l.isLit))

    def fromAlbedo(albedo: AssetName): Lit =
      new Lit(albedo, None, None, None, true)
  }

}

final class Texture(val assetName: AssetName, val amount: Double) {
  def hash: String =
    assetName.value + amount.toString()
}
object Texture {

  implicit val show: AsString[Texture] =
    AsString.create { texture =>
      s"""Texture(${texture.assetName.toString()}, ${texture.amount.toString()})"""
    }

  implicit val eq: EqualTo[Texture] =
    EqualTo.create {
      case (a, b) =>
        a.assetName === b.assetName &&
          a.amount === b.amount
    }

  def apply(assetName: AssetName, amount: Double): Texture =
    new Texture(assetName, amount)

  def unapply(t: Texture): Option[(AssetName, Double)] =
    Some((t.assetName, t.amount))
}
