package pirate

import indigo._

import pirate.subsystems.{CloudsAutomata, CloudsSubSystem}
import pirate.init.{Assets, InitialLoad, StartupData}
import pirate.game.{Model, ViewModel, View}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object PirateDemo extends IndigoDemo[StartupData, Model, ViewModel] {

  val config: GameConfig =
    GameConfig.default
      .withViewport(GameViewport.at720p)
      .withMagnification(2)

  val assets: Set[AssetType] =
    Assets.assets

  val fonts: Set[FontInfo] =
    Set(Assets.Fonts.fontInfo)

  val animations: Set[Animation] =
    Set(Assets.Clouds.cloudsAnimation)

  val subSystems: Set[SubSystem] =
    Set(
      CloudsAutomata.automata,                            // STEP 6
      CloudsSubSystem.init(config.screenDimensions.width) // STEP 5
    )

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, StartupData] =
    InitialLoad.setup(assetCollection, dice)

  def initialModel(startupData: StartupData): Model =
    Model.initialModel(config.screenDimensions)

  def initialViewModel(startupData: StartupData, model: Model): ViewModel =
    ViewModel.initialViewModel(startupData, config.screenDimensions)

  def updateModel(context: FrameContext, model: Model): GlobalEvent => Outcome[Model] =
    Model.update(context.gameTime, model, context.inputState, config.screenDimensions)

  def updateViewModel(context: FrameContext, model: Model, viewModel: ViewModel): Outcome[ViewModel] =
    Outcome(viewModel)

  def present(context: FrameContext, model: Model, viewModel: ViewModel): SceneUpdateFragment =
    View.drawBackground |+|
      View.sceneAudio |+|
      View.drawWater(viewModel) |+|
      View.drawForeground(viewModel, config.screenDimensions) |+|
      View.drawPirateWithRespawn(context.gameTime, model, viewModel.captain) // STEP 9
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
