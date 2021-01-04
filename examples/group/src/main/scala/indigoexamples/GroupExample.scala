package indigoexamples

import indigo._

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object GroupExample extends IndigoSandbox[Unit, Radians] {

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

  def setup(assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  def initialModel(startupData: Unit): Outcome[Radians] =
    Outcome(Radians.zero)

  def updateModel(context: FrameContext[Unit], model: Radians): GlobalEvent => Outcome[Radians] = {
    case FrameTick =>
      Outcome(model + Radians(0.01))
    case _ =>
      Outcome(model)
  }

  val graphic =
    Graphic(0, 0, 32, 32, 1, Material.Textured(assetName))
      .withCrop(32, 0, 32, 32)
      .withRef(16, 16)

  def present(context: FrameContext[Unit], model: Radians): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty.addGameLayerNodes(
        graphic.moveTo(16, 16),
        Group(
          graphic,
          Group(graphic)
            .moveBy(64, 64)
            .scaleBy(2, 2)
            .rotateBy(model * Radians(2.0))
        ).moveBy(config.screenDimensions.center)
          .scaleBy(2, 2)
          .rotateBy(model)
      )
    )
}
