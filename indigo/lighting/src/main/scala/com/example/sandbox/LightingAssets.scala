package com.example.sandbox

import indigo._
import indigo.shared.datatypes.Material

object LightingAssets {

  val junctionBoxAlbedo: AssetName   = AssetName("junctionbox_albedo")
  val junctionBoxEmission: AssetName = AssetName("junctionbox_emission")
  val junctionBoxNormal: AssetName   = AssetName("junctionbox_normal")
  val junctionBoxSpecular: AssetName = AssetName("junctionbox_specular")

  val junctionBoxMaterial: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      Some(junctionBoxEmission),
      Some(junctionBoxNormal),
      Some(junctionBoxSpecular)
    )

  def assets: Set[AssetType] =
    Set(
      AssetType.Tagged("atlas1")(
        AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
        AssetType.Image(junctionBoxEmission, AssetPath("assets/" + junctionBoxEmission.value + ".png")),
        AssetType.Image(junctionBoxNormal, AssetPath("assets/" + junctionBoxNormal.value + ".png")),
        AssetType.Image(junctionBoxSpecular, AssetPath("assets/" + junctionBoxSpecular.value + ".png"))
      )
    )

}
