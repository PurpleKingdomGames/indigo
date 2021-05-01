package com.example.lighting

import indigo._

import scala.scalajs.js.annotation._

import indigoextras.effectmaterials.LegacyEffects
import indigoextras.effectmaterials.Border
import indigoextras.effectmaterials.Thickness
import indigoextras.effectmaterials.Glow

@JSExportTopLevel("IndigoGame")
object EffectsExample extends IndigoSandbox[Unit, Unit] {

  val targetFPS: Int = 60

  private val magnificationLevel: Int = 2
  private val viewportWidth: Int      = 550
  private val viewportHeight: Int     = 400

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = targetFPS,
      clearColor = RGBA(0.0, 0.0, 0.2, 1.0),
      magnification = magnificationLevel
    )

  val assets: Set[AssetType] =
    EffectsAssets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animations: Set[Animation] =
    Set()

  val shaders: Set[Shader] =
    Set(LegacyEffects.entityShader)

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(())

  val graphic: Graphic =
    Graphic(Rectangle(0, 0, 64, 64), 1, EffectsAssets.junctionBoxMaterial)
      .withRef(20, 20)

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] = {
    val viewCenter: Point = config.viewport.giveDimensions(config.magnification).center + Point(0, -25)

    Outcome(
      SceneUpdateFragment(
        graphic // tint - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(0, -40)
          .modifyMaterial {
            case m: LegacyEffects => m.withTint(RGBA.Red)
            case m                => m
          },
        graphic // alpha - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-60, -40)
          .modifyMaterial {
            case m: LegacyEffects => m.withAlpha(0.5)
            case m                => m
          },
        graphic // saturation - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-30, -40)
          .modifyMaterial {
            case m: LegacyEffects => m.withSaturation(0.0)
            case m                => m
          },
        graphic //color overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(30, -40)
          .modifyMaterial {
            case m: LegacyEffects => m.withOverlay(Fill.Color(RGBA.Magenta.withAmount(0.75)))
            case m                => m
          },
        graphic // linear gradient overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(60, -40)
          .modifyMaterial {
            case m: LegacyEffects => m.withOverlay(Fill.LinearGradient(Point.zero, RGBA.Magenta, Point(40), RGBA.Cyan.withAmount(0.5)))
            case m                => m
          },
        graphic // radial gradient overlay - identical to ImageEffects material
          .moveTo(viewCenter)
          .moveBy(-60, 10)
          .modifyMaterial {
            case m: LegacyEffects => m.withOverlay(Fill.RadialGradient(Point(20), 10, RGBA.Magenta.withAmount(0.5), RGBA.Cyan.withAmount(0.25)))
            case m                => m
          },
        graphic // inner glow
          .moveTo(viewCenter)
          .moveBy(0, 10)
          .modifyMaterial {
            case m: LegacyEffects => m.withGlow(Glow(RGBA.Green, 2.0, 0.0))
            case m                => m
          },
        graphic // outer glow
          .moveTo(viewCenter)
          .moveBy(-30, 10)
          .modifyMaterial {
            case m: LegacyEffects => m.withGlow(Glow(RGBA.Blue, 0.0, 2.0))
            case m                => m
          },
        graphic // inner border
          .moveTo(viewCenter)
          .moveBy(30, 60)
          .modifyMaterial {
            case m: LegacyEffects => m.withBorder(Border(RGBA(1.0, 0.5, 0.0, 1.0), Thickness.Thick, Thickness.None))
            case m                => m
          },
        graphic // outer border
          .moveTo(viewCenter)
          .moveBy(60, 60)
          .modifyMaterial {
            case m: LegacyEffects => m.withBorder(Border(RGBA.Yellow, Thickness.None, Thickness.Thick))
            case m                => m
          },
        graphic // rotate & scale - standard transform
          .moveTo(viewCenter)
          .moveBy(30, 10)
          .rotateBy(Radians(0.2))
          .scaleBy(1.25, 1.25),
        graphic // flipped - standard transform
          .moveTo(viewCenter)
          .moveBy(60, 10)
          .flipHorizontal(true)
          .flipVertical(true)
      )
    )
  }
}

object EffectsAssets {

  val junctionBoxAlbedo: AssetName = AssetName("junctionbox_albedo")

  val junctionBoxMaterial: LegacyEffects =
    LegacyEffects(junctionBoxAlbedo)

  def assets: Set[AssetType] =
    Set(
      AssetType.Image(junctionBoxAlbedo, AssetPath("assets/" + junctionBoxAlbedo + ".png"))
    )

}
