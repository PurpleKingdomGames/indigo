package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object GraphicExample extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig = defaultGameConfig //.withMagnification(1)

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val shaders: Set[Shader] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  val graphic: Graphic =
    Graphic(0, 0, 256, 256, 1, Material.ImageEffects(AssetName("graphics")))
      .withRef(48, 48)

  val basic: Graphic =
    graphic
      .withCrop(128, 0, 96, 96)
      .moveTo(200, 200)
      .scaleBy(1.5, 1.5)
      .withRef(96, 96)

  def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(
          basic.modifyMaterial {
            case m: Material.ImageEffects => m.withAlpha(0.5)
            case m                        => m
          },
          basic
            .rotateTo(Radians(Math.PI / 8))
            .modifyMaterial {
              case m: Material.ImageEffects => m.withAlpha(0.75)
              case m                        => m
            },
          basic
            .rotateTo(Radians(Math.PI / 4))
            .modifyMaterial {
              case m: Material.ImageEffects => m.withAlpha(0.75)
              case m                        => m
            },
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 1, 100),
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 2, 100)
            .rotateTo(Radians(Math.PI / 4)),
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 3, 100)
            .flipHorizontal(true)
            .flipVertical(true),
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 1, 300)
            .modifyMaterial {
              case m: Material.ImageEffects => m.withAlpha(0.5)
              case m                        => m
            },
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 2, 300)
            .modifyMaterial {
              case m: Material.ImageEffects => m.withTint(RGBA.Red)
              case m                        => m
            },
          graphic
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 3, 300)
            .scaleBy(2.0, 2.0)
        )
    )
}
