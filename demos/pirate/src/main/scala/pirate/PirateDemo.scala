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
    Model.initialModel

  def update(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model] =
    Model.update(model, inputState)

  def initialViewModel(startupData: StartupData): Model => ViewModel =
    _ => ViewModel.initialViewModel(startupData, config.screenDimensions)

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
    View.present(viewModel, model.pirateState)

}
