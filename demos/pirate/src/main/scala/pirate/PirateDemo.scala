package pirate

import indigo._
import indigogame._

import pirate.subsystems.{CloudsAutomata, CloudsSubSystem}
import pirate.init.{Assets, InitialLoad, StartupData}
import pirate.game.{Model, ViewModel, View}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object PirateDemo extends IndigoGameBasic[StartupData, Model, ViewModel] {

  val config: GameConfig =
    GameConfig.default
      .withViewport(GameViewport.at720p)
      .withMagnification(2)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(Assets.Fonts.fontInfo)

  val animations: Set[Animation] =
    Set(
      Assets.Clouds.cloudsAnimation1,
      Assets.Clouds.cloudsAnimation2,
      Assets.Clouds.cloudsAnimation3
    )

  val subSystems: Set[SubSystem] =
    Set(
      CloudsAutomata.automata,                            // STEP 6
      CloudsSubSystem.init(config.screenDimensions.width) // STEP 5
    )

  def setup(assetCollection: AssetCollection, flags: Map[String, String]): Startup[StartupErrors, StartupData] =
    InitialLoad.setup(assetCollection)

  def initialModel(startupData: StartupData): Model =
    Model.initialModel(config.screenDimensions)

  def update(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model] =
    Model.update(gameTime, model, inputState, config.screenDimensions)

  def initialViewModel(startupData: StartupData): Model => ViewModel =
    _ => ViewModel.initialViewModel(startupData, config.screenDimensions)

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    Outcome(viewModel)

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
    View.drawBackground |+|
      View.sceneAudio |+|
      View.drawWater(viewModel) |+|
      View.drawForeground(viewModel, config.screenDimensions) |+|
      View.drawPirateWithRespawn(gameTime, model, viewModel.captain) // STEP 9
  // View.drawBackground |+|
  //   View.sceneAudio |+|
  //   View.drawWater(viewModel) |+|
  //   View.drawForeground(viewModel, config.screenDimensions) |+|
  //   View.drawPirate(model, viewModel.captain) // STEP 8
  // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) |+|
  //   View.drawForeground(viewModel, config.screenDimensions) // STEP 7
  // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) // STEP 4
  // View.drawBackground |+| View.sceneAudio // STEP 3
  // View.drawBackground // STEP 2
  // noRender // STEP 1

}
