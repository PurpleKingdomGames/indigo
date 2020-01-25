package pirate

import indigo._
import indigoexts.entrypoint._

object PirateDemo extends IndigoGameBasic[StartupData, Model, ViewModel] {

  val config: GameConfig =
    GameConfig.default
      .withViewport(GameViewport.at720p)
      .withMagnification(2)
      .withFrameRate(30)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set()

  val animations: Set[Animation] =
    Set()

  val subSystems: Set[SubSystem] =
    Set(CloudsSubSystem.init(config.screenDimensions.width))

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData] =
    InitialLoad.setup(assetCollection)

  def initialModel(startupData: StartupData): Model =
    Model()

  def update(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model] =
    _ => Outcome(model)

  def initialViewModel(startupData: StartupData): Model => ViewModel =
    _ =>
      ViewModel(
        startupData.waterReflections
          .changeCycle(CycleLabel("big"))
          .withRef(85, 0)
          .moveTo(config.screenDimensions.horizontalCenter, config.screenDimensions.verticalCenter + 5)
      )

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(Assets.backgroundGraphic)
      .addGameLayerNodes(viewModel.waterRelfections.play())

}

final case class Model()
final case class ViewModel(waterRelfections: Sprite)
