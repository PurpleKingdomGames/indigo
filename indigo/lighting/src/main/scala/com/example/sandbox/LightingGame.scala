package com.example.sandbox

import indigo._
import indigoexts.entrypoint._

object LightingGame extends IndigoGameBasic[Unit, Unit, Unit] {

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
    Graphic(Rectangle(0, 0, 64, 64), 1, LightingAssets.junctionBoxMaterial)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center)

  def orbitingLight: Signal[PointLight] =
    (Signal.SinWave |*| Signal.CosWave).map {
      case (s, c) =>
        val x = ((s * 150) + config.viewport.center.x).toInt
        val y = ((c * 150) + config.viewport.center.y).toInt

        PointLight.default
          .moveTo(Point(x, y))
          .withAttenuation(100)
          .withColor(RGB.Magenta)
    }

  def pulsingLight: Signal[PointLight] =
    Signal.SinWave.map { s =>
      PointLight.default
        .moveTo(config.viewport.center + Point(50, -50))
        .withAttenuation((Math.abs(s) * 60).toInt)
        .withColor(RGB.Cyan)
    }

  def present(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        graphic,
        graphic.moveBy(-30, 0),
        graphic.moveBy(30, 0)
      )
      .withAmbientLight(RGBA.White.withAmount(0.1))
      .withLights(
        orbitingLight.affectTime(4).at(gameTime.running),
        pulsingLight.affectTime(3).at(gameTime.running)
      )
}

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
