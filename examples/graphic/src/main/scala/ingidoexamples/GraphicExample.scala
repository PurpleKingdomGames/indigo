package ingidoexamples

import indigo._
import indigoexts.entrypoint._

object GraphicExample extends IndigoGameBasic[Unit, Unit, Unit] {

  val config: GameConfig = defaultGameConfig //.withMagnification(1)

  val assets: Set[AssetType] = Set(AssetType.Image("graphics", "assets/graphics.png"))

  val fonts: Set[FontInfo] = Set()

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set()

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, Unit] =
    Startup.Success(())

  def initialModel(startupData: Unit): Unit =
    ()

  def update(gameTime: GameTime, model: Unit, dice: Dice): GlobalEvent => Outcome[Unit] =
    _ => Outcome(model)

  def initialViewModel(startupData: Unit): Unit => Unit = _ => ()

  def updateViewModel(gameTime: GameTime, model: Unit, viewModel: Unit, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[Unit] =
    Outcome(())

  def present(gameTime: GameTime, model: Unit, viewModel: Unit, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        Group(
          Graphic(0, 0, 256, 256, 1, "graphics")
            .withRef(48, 48)
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 1, 0),
          Graphic(0, 0, 256, 256, 1, "graphics")
            .withRef(48, 48)
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 2, 0)
            .rotate(Radians(Math.PI / 4)),
          Graphic(0, 0, 256, 256, 1, "graphics")
            .withRef(48, 48)
            .withCrop(128, 0, 96, 96)
            .moveTo(137 * 3, 0)
            .scaleBy(0.5, 0.5)
        ).moveBy(0, 200)
          .rotateBy(Radians(0.1d))
          .scaleBy(0.5d, 0.5d)
      )
}
