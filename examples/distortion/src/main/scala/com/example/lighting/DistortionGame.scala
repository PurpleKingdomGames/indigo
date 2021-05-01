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

  val viewCenter: Point =
    config.viewport.giveDimensions(magnificationLevel).center

  val assets: Set[AssetType] =
    DistortionAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animationsKey: AnimationKey =
    AnimationKey("anims")

  val animations: Set[Animation] =
    Set()

  val shaders: Set[Shader] =
    Set(
      Refraction.entityShader,
      Refraction.blendShader
    )

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, DistortionAssets.junctionBoxMaterial)
      .withRef(20, 20)
      .moveTo(viewCenter)

  val imageLight: Graphic =
    Graphic(Rectangle(0, 0, 320, 240), 1, DistortionAssets.imageLightMaterial)
      .moveBy(-14, -60)

  val distortion: Graphic =
    Graphic(Rectangle(0, 0, 240, 240), 1, DistortionAssets.normalMapMaterial)
      .scaleBy(0.5, 0.5)
      .withRef(120, 120)

  val background: Graphic =
    Graphic(Rectangle(0, 0, 790, 380), 1, DistortionAssets.foliageMaterial)

  def sliding: Signal[Graphic] =
    Signal.SmoothPulse.map { d =>
      distortion.moveTo(Point(70, 70 + (50 * d).toInt))
    }

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        background,
        graphic,
        graphic.moveBy(-60, 0),
        graphic.moveBy(-30, 0),
        graphic.moveBy(30, 0),
        graphic.moveBy(60, 0)
      ).addLayer(
        Layer(imageLight).withBlending(Blending.Lighting(RGBA(0.2, 0.5, 0.3, 0.5)))
      )
      .addLayer(
        Layer(
            distortion.moveTo(viewCenter + Point(50, 0)),
            sliding.affectTime(0.3).at(context.gameTime.running)
          ).withBlending(
            Refraction.blending(
              Signal.SmoothPulse
                .map(d => 0.25 * d)
                .affectTime(0.25)
                .at(context.running)
            )
          )
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
  val normalName: AssetName        = AssetName("normal-map")

  val normalMapMaterial: RefractionEntity =
    RefractionEntity(normalName)

  val junctionBoxMaterial: Material.Bitmap =
    Material.Bitmap(junctionBoxAlbedo)

  val foliageMaterial: Material.Bitmap =
    Material.Bitmap(foliageName)

  val imageLightMaterial: Material.Bitmap =
    Material.Bitmap(imageLightName)

  def assets: Set[AssetType] =
    Set(
      AssetType.Tagged("atlas1")(
        AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo + ".png")),
        AssetType.Image(junctionBoxEmission.assetName, AssetPath("assets/" + junctionBoxEmission.assetName + ".png")),
        AssetType.Image(junctionBoxNormal.assetName, AssetPath("assets/" + junctionBoxNormal.assetName + ".png")),
        AssetType.Image(junctionBoxSpecular.assetName, AssetPath("assets/" + junctionBoxSpecular.assetName + ".png")),
        AssetType.Image(imageLightName, AssetPath("assets/" + imageLightName + ".png")),
        AssetType.Image(foliageName, AssetPath("assets/" + foliageName + ".png")),
        AssetType.Image(normalName, AssetPath("assets/" + normalName + ".png"))
      )
    )

}
