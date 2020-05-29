package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object GraphicExample extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig = defaultGameConfig //.withMagnification(1)

  val assets: Set[AssetType] = Set(AssetType.Image(AssetName("graphics"), AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def updateModel(context: FrameContext, model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  val graphic: Graphic =
    Graphic(0, 0, 256, 256, 1, Material.Textured(AssetName("graphics")))
      .withRef(48, 48)

  val basic: Graphic =
    graphic
      .withCrop(128, 0, 96, 96)
      .moveTo(200, 200)
      .scaleBy(1.5, 1.5)
      .withRef(96, 96)

  def present(context: FrameContext, model: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        basic.withAlpha(0.5),
        basic
          .rotate(Radians(Math.PI / 8))
          .withAlpha(0.75),
        basic
          .rotate(Radians(Math.PI / 4))
          .withAlpha(0.75),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 1, 100),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 2, 100)
          .rotate(Radians(Math.PI / 4)),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 3, 100)
          .flipHorizontal(true)
          .flipVertical(true),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 1, 300)
          .withAlpha(0.5),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 2, 300)
          .withTint(RGBA.Red),
        graphic
          .withCrop(128, 0, 96, 96)
          .moveTo(137 * 3, 300)
          .scaleBy(2.0, 2.0)
      )
}
