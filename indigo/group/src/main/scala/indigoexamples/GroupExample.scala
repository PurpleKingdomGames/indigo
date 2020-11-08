package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object GroupExample extends IndigoSandbox[Unit, Unit] {

  val config: GameConfig =
    GameConfig.default

  val assetName: AssetName =
    AssetName("graphics")

  val assets: Set[AssetType] =
    Set(AssetType.Image(assetName, AssetPath("assets/graphics.png")))

  val fonts: Set[FontInfo] =
    Set()

  val animations: Set[Animation] =
    Set()

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def present(context: FrameContext[Unit], model: Unit): SceneUpdateFragment =
    SceneUpdateFragment.empty.addGameLayerNodes(
      Group(
        Graphic(0, 0, 32, 32, 1, Material.Textured(assetName))
          .withCrop(32, 0, 32, 32)
          .withRef(16, 16),
        Group(
          Graphic(0, 0, 32, 32, 1, Material.Textured(assetName))
            .withCrop(32, 0, 32, 32)
            .withRef(16, 16)
        ).moveBy(32, 32)
          .scaleBy(2, 2)
          .rotateBy(Radians.TAUby4)
      ).moveBy(config.screenDimensions.center)
        .scaleBy(2, 2)
        .rotateBy(Radians.TAUby4)
    )
}
