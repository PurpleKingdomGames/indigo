package com.example.lighting

import indigo._

import scala.scalajs.js.annotation._

import indigoextras.effectmaterials.Refraction
import indigoextras.effectmaterials.RefractionEntity

@JSExportTopLevel("IndigoGame")
object DistortionGame extends IndigoSandbox[Unit, Unit] {

  val targetFPS: Int = 60

  private val magnificationLevel: Int = 3
  private val viewportWidth: Int      = 228 * magnificationLevel
  private val viewportHeight: Int     = 128 * magnificationLevel

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = RGBA(0.0, 0.0, 0.2, 1.0),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    DistortionAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animationsKey: AnimationKey =
    AnimationKey("anims")

  val animations: Set[Animation] =
    Set()

  val shaders: Set[Shader] =
    Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, DistortionAssets.junctionBoxMaterialOn)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center)

  val imageLight: Graphic =
    Graphic(Rectangle(0, 0, 320, 240), 1, DistortionAssets.imageLightMaterial)
      .moveBy(-14, -60)

  val distortion: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, DistortionAssets.smoothBumpMaterial)
      .moveTo(config.viewport.giveDimensions(config.magnification).center)
      .withRef(32, 32)

  val background: Graphic =
    Graphic(Rectangle(0, 0, 790, 380), 1, DistortionAssets.foliageMaterial)

  def orbiting(distance: Int): Signal[Graphic] =
    Signal.Orbit(config.viewport.giveDimensions(config.magnification).center, distance.toDouble).map { vec =>
      distortion.moveTo(vec.toPoint)
    }

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        background,
        graphic,
        graphic.moveBy(-60, 0).withMaterial(DistortionAssets.junctionBoxMaterialOff),
        graphic.moveBy(-30, 0).withMaterial(DistortionAssets.junctionBoxMaterialGlass),
        graphic.moveBy(30, 0).withMaterial(DistortionAssets.junctionBoxMaterialFlat),
        graphic.moveBy(60, 0).withMaterial(DistortionAssets.junctionBoxMaterialFlat.withLighting(LightingModel.Unlit))
      ).withLights(
          AmbientLight(RGBA.White.withAmount(0.1)),
          PointLight.default
            .moveTo(config.viewport.center + Point(50, 0))
            .withFalloff(Falloff.SmoothQuadratic(0, 50))
            .withColor(RGBA.Magenta.withAmount(0.4)),
          DirectionLight(RGBA.Green.withAmount(1.2), RGBA.Green, Radians.fromDegrees(30))
        )
        .addLayer(
          Layer(imageLight).withBlending(Blending.Lighting(RGBA.White.withAlpha(0.75)))
        )
        .addLayer(
          Layer(
            distortion.modifyMaterial {
              case m: Material.ImageEffects => m.withAlpha(1.0)
              case m                        => m
            },
            orbiting(40).affectTime(0.25).at(context.gameTime.running)
          ).withBlending(Refraction.blending(1.0))
        )
    )
}

object DistortionAssets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")
  val junctionBoxEmission: Texture = Texture(AssetName("junctionbox_emission"), 1.0d)
  val junctionBoxNormal: Texture   = Texture(AssetName("junctionbox_normal"), 1.0d)
  val junctionBoxSpecular: Texture = Texture(AssetName("junctionbox_specular"), 1.0d)
  val imageLightName: AssetName    = AssetName("light_texture")
  val foliageName: AssetName       = AssetName("foliage")
  val smoothBumpName: AssetName    = AssetName("smooth-bump2")

  val normalMapMaterial: RefractionEntity =
    RefractionEntity(smoothBumpName)

  val junctionBoxMaterialOn: Material.Bitmap =
    Material.Bitmap(
      junctionBoxAlbedo,
      LightingModel.Lit(
        Some(junctionBoxEmission),
        Some(junctionBoxNormal),
        Some(junctionBoxSpecular)
      )
    )

  val junctionBoxMaterialGlass: Material.Bitmap =
    Material.Bitmap(
      junctionBoxAlbedo,
      LightingModel.Lit(
        Some(junctionBoxEmission),
        Some(junctionBoxNormal),
        Some(junctionBoxSpecular)
      )
    )

  val junctionBoxMaterialOff: Material.Bitmap =
    Material.Bitmap(
      junctionBoxAlbedo,
      LightingModel.Lit(
        None,
        Some(junctionBoxNormal),
        Some(junctionBoxSpecular)
      )
    )

  val junctionBoxMaterialFlat: Material.Bitmap =
    Material.Bitmap(junctionBoxAlbedo, LightingModel.Lit.flat)

  val foliageMaterial: Material.Bitmap =
    Material.Bitmap(foliageName, LightingModel.Lit.flat)

  val imageLightMaterial: Material.Bitmap =
    Material.Bitmap(imageLightName)

  val smoothBumpMaterial: Material.Bitmap =
    Material.Bitmap(smoothBumpName)

  def assets: Set[AssetType] =
    Set(
      AssetType.Tagged("atlas1")(
        AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
        AssetType.Image(junctionBoxEmission.assetName, AssetPath("assets/" + junctionBoxEmission.assetName.value + ".png")),
        AssetType.Image(junctionBoxNormal.assetName, AssetPath("assets/" + junctionBoxNormal.assetName.value + ".png")),
        AssetType.Image(junctionBoxSpecular.assetName, AssetPath("assets/" + junctionBoxSpecular.assetName.value + ".png")),
        AssetType.Image(imageLightName, AssetPath("assets/" + imageLightName.value + ".png")),
        AssetType.Image(foliageName, AssetPath("assets/" + foliageName.value + ".png")),
        AssetType.Image(smoothBumpName, AssetPath("assets/" + smoothBumpName.value + ".png"))
      )
    )

}
