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
      clearColor = ClearColor(0.0, 0.0, 0.2, 1.0),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    LightingAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animationsKey: AnimationKey =
    AnimationKey("anims")

  val animations: Set[Animation] =
    Set(
      Animation(
        animationsKey,
        LightingAssets.trafficLightsMaterial,
        Frame(Rectangle(0, 0, 64, 64), Millis(500)),
        Frame(Rectangle(64, 0, 64, 64), Millis(500)),
        Frame(Rectangle(0, 64, 64, 64), Millis(500))
      )
    )

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, LightingAssets.junctionBoxMaterialOn)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center)

  def orbitingLight(distance: Int): Signal[PointLight] =
    Signal.Orbit(config.viewport.center, distance.toDouble).map { vec =>
      PointLight.default
        .moveTo(vec.toPoint)
        .withAttenuation(150)
        .withColor(RGB.Magenta)
    }

  def pulsingLight: Signal[PointLight] =
    Signal.SmoothPulse.map { amount =>
      PointLight.default
        .moveTo(config.viewport.center + Point(30, -60))
        .withAttenuation((amount * 70).toInt)
        .withColor(RGB.Cyan)
    }

  def present(context: FrameContext[Unit], model: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        graphic,
        graphic.moveBy(-60, 0).withMaterial(LightingAssets.junctionBoxMaterialOff),
        graphic.moveBy(-30, 0).withMaterial(LightingAssets.junctionBoxMaterialGlass),
        graphic.moveBy(30, 0).withMaterial(LightingAssets.junctionBoxMaterialFlat),
        graphic.moveBy(60, 0).withMaterial(LightingAssets.junctionBoxMaterialFlat.unlit)
      )
      .withAmbientLight(RGBA.White.withAmount(0.1))
      .withLights(
        // PointLight.default
        //   .moveTo(config.viewport.center + Point(50, 0))
        //   .withAttenuation(50)
        //   .withColor(RGB.Green),
        // PointLight.default
        //   .moveTo(config.viewport.center + Point(-50, 0))
        //   .withAttenuation(50)
        //   .withColor(RGB.Red),
        orbitingLight(120).affectTime(0.25).at(context.gameTime.running),
        pulsingLight.affectTime(0.5).at(context.gameTime.running)
        // DirectionLight(30, RGB.Green, 1.2, Radians.fromDegrees(30)),
        // SpotLight.default
        //   .withColor(RGB.Yellow)
        //   .moveTo(config.viewport.center + Point(-150, -60))
        //   .rotateBy(Radians.fromDegrees(45))
        //   .withHeight(25)
        //   .withPower(1.5)
      )
      .addGameLayerNodes(
        Sprite(BindingKey("lights animation"), 0, 0, 1, animationsKey).play()
      )
  // .addGameLayerNodes(
  //   graphic
  //     .moveTo(config.viewport.giveDimensions(config.magnification).center.x, 30)
  //     .withCrop(10, 10, 20, 20)
  // )
}

object LightingAssets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")
  val junctionBoxEmission: Texture = Texture(AssetName("junctionbox_emission"), 1.0d)
  val junctionBoxNormal: Texture   = Texture(AssetName("junctionbox_normal"), 1.0d)
  val junctionBoxSpecular: Texture = Texture(AssetName("junctionbox_specular"), 1.0d)
  val trafficLightsName: AssetName = AssetName("trafficlights")

  val junctionBoxMaterialOn: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      Some(junctionBoxEmission),
      Some(junctionBoxNormal),
      Some(junctionBoxSpecular)
    )

  val junctionBoxMaterialGlass: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      Some(junctionBoxEmission),
      Some(junctionBoxNormal),
      Some(junctionBoxSpecular)
    )

  val junctionBoxMaterialOff: Material.Lit =
    Material.Lit(
      junctionBoxAlbedo,
      None,
      Some(junctionBoxNormal),
      Some(junctionBoxSpecular)
    )

  val junctionBoxMaterialFlat: Material.Textured =
    Material.Textured(junctionBoxAlbedo).lit

  val trafficLightsMaterial: Material.Lit =
    Material.Lit(
      trafficLightsName,
      Some(Texture(trafficLightsName, 1.0d)),
      None,
      None
    )

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
