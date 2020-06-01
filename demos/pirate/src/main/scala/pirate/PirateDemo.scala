package pirate

import indigo._

import pirate.subsystems.{CloudsAutomata, CloudsSubSystem}
import pirate.init.{Assets, InitialLoad, StartupData}
import pirate.game.{Model, ViewModel, View}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object PirateDemo extends IndigoDemo[Rectangle, StartupData, Model, ViewModel] {

  def boot(flags: Map[String, String]): BootResult[Rectangle] = {
    val config =
      GameConfig.default
        .withViewport(GameViewport.at720p)
        .withMagnification(2)

    BootResult(
      config,
      config.screenDimensions
    ).withAssets(Assets.assets)
      .withFonts(Assets.Fonts.fontInfo)
      .withAnimations(Assets.Clouds.cloudsAnimation)
      .withSubSystems(
        CloudsAutomata.automata,                            // STEP 6
        CloudsSubSystem.init(config.screenDimensions.width) // STEP 5
      )
  }

  def setup(bootData: Rectangle, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData] =
    InitialLoad.setup(bootData, assetCollection, dice)

  def initialModel(startupData: StartupData): Model =
    Model.initialModel(startupData.screenDimensions)

  def initialViewModel(startupData: StartupData, model: Model): ViewModel =
    ViewModel.initialViewModel(startupData, startupData.screenDimensions)

  def updateModel(context: FrameContext, model: Model): GlobalEvent => Outcome[Model] =
    Model.update(context.gameTime, model, context.inputState)

  def updateViewModel(context: FrameContext, model: Model, viewModel: ViewModel): Outcome[ViewModel] =
    Outcome(viewModel)

  def present(context: FrameContext, model: Model, viewModel: ViewModel): SceneUpdateFragment =
    View.drawBackground |+|
      View.sceneAudio |+|
      View.drawWater(viewModel) |+|
      View.drawForeground(viewModel, model.screenDimensions) |+|
      View.drawPirateWithRespawn(context.gameTime, model, viewModel.captain) // STEP 9
  // View.drawBackground |+|
  //   View.sceneAudio |+|
  //   View.drawWater(viewModel) |+|
  //   View.drawForeground(viewModel, model.screenDimensions) |+|
  //   View.drawPirate(model, viewModel.captain) // STEP 8
  // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) |+|
  //   View.drawForeground(viewModel, model.screenDimensions) // STEP 7
  // View.drawBackground |+| View.sceneAudio |+| View.drawWater(viewModel) // STEP 4
  // View.drawBackground |+| View.sceneAudio // STEP 3
  // View.drawBackground // STEP 2
  // noRender // STEP 1

}
