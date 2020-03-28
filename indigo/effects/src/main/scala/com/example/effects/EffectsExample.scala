package com.example.lighting

import indigo._
import indigoexts.entrypoint._

object EffectsExample extends IndigoGameBasic[Unit, Unit, Unit] {

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
    EffectsAssets.assets

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
    Graphic(Rectangle(0, 0, 64, 64), 1, EffectsAssets.junctionBoxMaterial)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center + Point(0, -25))

  def present(gameTime: GameTime, model: Unit, viewModel: Unit, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        graphic
          .withEffects(
            Effects.default.withTint(RGBA.Magenta)
          ),
        graphic
          .moveBy(-60, 0)
          .withEffects(
            Effects.default.withColorOverlay(Overlay.Color(RGBA.Magenta.withAmount(0.75)))
          ),
        graphic
          .moveBy(-30, 0)
          .withEffects(
            Effects.default
              .withGradiantOverlay(
                Overlay.LinearGradiant(Point.zero, RGBA.Magenta, Point(64, 64), RGBA.Cyan.withAmount(0.5))
              )
          ),
        graphic
          .moveBy(30, 0)
          .withEffects(
            Effects.default
              .withBorder(Border(RGBA.Yellow, Thickness.Thick, Thickness.None))
          ),
        graphic
          .moveBy(60, 0)
          .withEffects(
            Effects.default
              .withBorder(Border(RGBA.Yellow, Thickness.None, Thickness.Thick))
          ),
        graphic
          .moveBy(-60, 50)
          .withEffects(
            Effects.default
              .withBorder(Border(RGBA.Yellow, Thickness.Thick, Thickness.Thick))
          ),
        graphic
          .moveBy(0, 50)
          .withEffects(
            Effects.default
              .withGlow(Glow(RGBA.Yellow, 10.0, 0.0))
          ),
        graphic
          .moveBy(-30, 50)
          .withEffects(
            Effects.default
              .withGlow(Glow(RGBA.Yellow, 0.0, 10.0))
          ),
        graphic
          .moveBy(30, 50)
          .withEffects(
            Effects.default
              .withGlow(Glow(RGBA.Yellow, 10.0, 10.0))
          ),
        graphic
          .moveBy(60, 50)
          .withEffects(
            Effects.default
              .withAlpha(0.5)
              .withFlip(Flip(true, true))
          )
      )
}

object EffectsAssets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")

  val junctionBoxMaterial: Material.Textured =
    Material.Textured(junctionBoxAlbedo)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo.value + ".png"))
    )

}
