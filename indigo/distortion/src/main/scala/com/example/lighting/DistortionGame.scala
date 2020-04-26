package com.example.lighting

import indigo._
import indigoexts.entrypoint._

object DistortionGame extends IndigoGameBasic[Unit, Unit, Unit] {

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
    DistortionAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animationsKey: AnimationKey =
    AnimationKey("anims")

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  def initialViewModel(startupData: Unit): Unit => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState, dice: Dice): Outcome[Unit] =
    Outcome(viewModel)

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

  def present(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        background,
        graphic,
        graphic.moveBy(-60, 0).withMaterial(DistortionAssets.junctionBoxMaterialOff),
        graphic.moveBy(-30, 0).withMaterial(DistortionAssets.junctionBoxMaterialGlass),
        graphic.moveBy(30, 0).withMaterial(DistortionAssets.junctionBoxMaterialFlat),
        graphic.moveBy(60, 0).withMaterial(DistortionAssets.junctionBoxMaterialFlat.unlit)
      )
      .withAmbientLight(RGBA.White.withAmount(0.1))
      .withLights(
        PointLight.default
          .moveTo(config.viewport.center + Point(50, 0))
          .withAttenuation(50)
          .withColor(RGB.Magenta)
          .withPower(0.4),
        DirectionLight(30, RGB.Green, 1.2, Radians.fromDegrees(30))
      )
      .addLightingLayerNodes(
        imageLight
      )
      .addDistortionLayerNodes(
        distortion.withAlpha(1.0),
        orbiting(40).affectTime(0.25).at(gameTime.running)
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

  val foliageMaterial: Material.Textured =
    Material.Textured(foliageName).lit

  val imageLightMaterial: Material.Textured =
    Material.Textured(imageLightName)

  val smoothBumpMaterial: Material.Textured =
    Material.Textured(smoothBumpName)

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
