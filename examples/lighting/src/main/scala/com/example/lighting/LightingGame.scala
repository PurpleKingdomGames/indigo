package com.example.lighting

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object LightingGame extends IndigoSandbox[Unit, Unit] {

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
    LightingAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animationsKey: AnimationKey =
    AnimationKey("anims")

  val shaders: Set[Shader] =
    Set()

  val animations: Set[Animation] =
    Set(
      Animation(
        animationsKey,
        Frame(Rectangle(0, 0, 64, 64), Millis(500)),
        Frame(Rectangle(64, 0, 64, 64), Millis(500)),
        Frame(Rectangle(0, 64, 64, 64), Millis(500))
      )
    )

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, LightingAssets.junctionBoxMaterialOn)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center)

  def orbitingLight(distance: Int): Signal[PointLight] =
    Signal.Orbit(viewCenter, distance.toDouble).map { vec =>
      PointLight.default
        .moveTo(vec.toPoint)
        .withFalloff(Falloff.SmoothQuadratic(0, 50))
        .withIntensity(2)
        .withColor(RGBA.Magenta)
        .withSpecular(RGBA.Magenta)
    }

  def pulsingLight: Signal[PointLight] =
    Signal.SmoothPulse.map { amount =>
      PointLight.default
        .moveTo(viewCenter + Point(30, -60))
        .withFalloff(Falloff.SmoothQuadratic(0, (amount * 100).toInt))
        .withColor(RGBA.Cyan)
        .withSpecular(RGBA.Cyan)
    }

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        graphic,
        graphic.moveBy(-60, 0).withMaterial(LightingAssets.junctionBoxMaterialOff),
        graphic.moveBy(-30, 0).withMaterial(LightingAssets.junctionBoxMaterialOn),
        graphic.moveBy(30, 0).withMaterial(LightingAssets.junctionBoxMaterialFlat),
        graphic.moveBy(60, 0).withMaterial(LightingAssets.junctionBoxMaterialFlat.withLighting(LightingModel.Unlit)),
        Sprite(BindingKey("lights animation"), 0, 0, 1, animationsKey, LightingAssets.trafficLightsMaterial).play()
      ).withLights(
        AmbientLight(RGBA.White.withAmount(0.1)),
        PointLight.default
          .moveTo(viewCenter + Point(50, 0))
          .withFalloff(Falloff.SmoothQuadratic(75))
          .withIntensity(1)
          .withColor(RGBA.Green),
        PointLight.default
          .moveTo(viewCenter + Point(-50, 0))
          .withFalloff(Falloff.Quadratic(0, 50))
          .withIntensity(100)
          .withColor(RGBA.Red),
        orbitingLight(60).affectTime(0.25).at(context.gameTime.running),
        pulsingLight.affectTime(0.5).at(context.gameTime.running),
        DirectionLight(RGBA.Green.withAlpha(0.1), RGBA.White.withAlpha(0.5), Radians.fromDegrees(30)),
        SpotLight.default
          .withColor(RGBA.Yellow)
          .withSpecular(RGBA.Yellow)
          .moveTo(viewCenter + Point(-50, -30))
          .rotateBy(Radians.fromDegrees(135))
          .withFalloff(Falloff.None(15, 100))
      )
    )
}

object LightingAssets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")
  val junctionBoxEmission: Texture = Texture(AssetName("junctionbox_emission"), 1.0d)
  val junctionBoxNormal: Texture   = Texture(AssetName("junctionbox_normal"), 1.0d)
  val junctionBoxSpecular: Texture = Texture(AssetName("junctionbox_specular"), 1.0d)
  val trafficLightsName: AssetName = AssetName("trafficlights")

  val lightingModel: LightingModel.Lit =
    LightingModel.Lit(
      Some(junctionBoxEmission),
      Some(junctionBoxNormal),
      Some(junctionBoxSpecular)
    )

  val junctionBoxMaterialOn: Material.Bitmap =
    Material.Bitmap(junctionBoxAlbedo, lightingModel)

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

  val trafficLightsMaterial: Material.Bitmap =
    Material.Bitmap(trafficLightsName, LightingModel.Unlit)

  def assets: Set[AssetType] =
    Set(
      AssetType.Tagged("atlas1")(
        AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png")),
        AssetType.Image(junctionBoxEmission.assetName, AssetPath("assets/" + junctionBoxEmission.assetName.value + ".png")),
        AssetType.Image(junctionBoxNormal.assetName, AssetPath("assets/" + junctionBoxNormal.assetName.value + ".png")),
        AssetType.Image(junctionBoxSpecular.assetName, AssetPath("assets/" + junctionBoxSpecular.assetName.value + ".png")),
        AssetType.Image(trafficLightsName, AssetPath("assets/" + trafficLightsName.value + ".png"))
      )
    )

}
