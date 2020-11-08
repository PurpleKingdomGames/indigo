package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object GroupExample extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig = defaultGameConfig

  val assetName: AssetName = AssetName("graphics")

  val assets: Set[AssetType] = Set(AssetType.Image(assetName, AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def present(context: FrameContext[Unit], model: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(
      Group(
        Graphic(0, 0, 256, 256, 1, Material.Textured(assetName)).moveTo(64, 10).moveBy(-50, -50),
        Graphic(0, 0, 32, 32, 1, Material.Textured(assetName)).withCrop(32, 0, 32, 32).moveBy(-50, -50),
        Graphic(0, 0, 128, 128, 1, Material.Textured(assetName))
          .moveTo(0, 128)
          .withCrop(128, 0, 128, 128)
          .withTint(0, 1, 1)
          .withAlpha(0.5)
          .moveBy(-50, -50)
      ).moveTo(100, 100)
    )
}
