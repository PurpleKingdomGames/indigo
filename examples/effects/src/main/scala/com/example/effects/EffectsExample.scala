package com.example.lighting

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object EffectsExample extends IndigoSandbox[Unit, Unit] {

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

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, EffectsAssets.junctionBoxMaterial)
      .withRef(20, 20)
      .moveTo(config.viewport.giveDimensions(config.magnification).center + Point(0, -25))

  def present(context: FrameContext[Unit], model: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        graphic
          .withTint(RGBA.Magenta),
        graphic
          .moveBy(-60, 0)
          .withOverlay(Overlay.Color(RGBA.Magenta.withAmount(0.75))),
        graphic
          .moveBy(-30, 0)
          .withOverlay(
            Overlay.LinearGradiant(Point.zero, RGBA.Magenta, Point(64, 64), RGBA.Cyan.withAmount(0.5))
          ),
        graphic
          .moveBy(30, 0)
          .withBorder(Border(RGBA.Yellow, Thickness.Thick, Thickness.None)),
        graphic
          .moveBy(60, 0)
          .withBorder(Border(RGBA.Red, Thickness.None, Thickness.Thick)),
        graphic
          .moveBy(-60, 50)
          .withBorder(Border(RGBA(1.0, 0.5, 0.0, 1.0), Thickness.Thick, Thickness.Thick)),
        graphic
          .moveBy(0, 50)
          .withGlow(Glow(RGBA.Green, 2.0, 0.0)),
        graphic
          .moveBy(-30, 50)
          .withGlow(Glow(RGBA.Blue, 0.0, 2.0)),
        graphic
          .moveBy(30, 50)
          .withGlow(Glow(RGBA.Cyan, 2.0, 2.0)),
        graphic
          .withRef(32, 32)
          .moveBy(48, 39)
          .withAlpha(0.5)
          .flipHorizontal(true)
          .flipVertical(true)
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
