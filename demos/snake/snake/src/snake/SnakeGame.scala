package snake

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import snake.model.{ControlScheme, GameModel, ViewModel}
import snake.init.{GameAssets, SnakeStartupData, ViewConfig}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[ViewConfig, SnakeStartupData, GameModel, ViewModel] {

  def initialScene(bootData: ViewConfig): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: ViewConfig): NonEmptyList[Scene[SnakeStartupData, GameModel, ViewModel]] =
    NonEmptyList(StartScene, ControlsScene, GameScene, GameOverScene)

  val eventFilters: EventFilters =
    EventFilters.Restricted

  def boot(flags: Map[String, String]): Outcome[BootResult[ViewConfig]] =
    Outcome {
      val viewConfig: ViewConfig =
        ViewConfig.default

      val assetPath: String =
        flags.getOrElse("baseUrl", "")

      val config =
        GameConfig(
          viewport = viewConfig.viewport,
          frameRate = 60,
          clearColor = RGBA.Black,
          magnification = viewConfig.magnificationLevel
        )

      BootResult(config, viewConfig)
        .withAssets(GameAssets.assets(assetPath))
        .withFonts(GameAssets.fontInfo)
        .withSubSystems(
          Set(FPSCounter(GameAssets.fontKey, Point(5, 5), 60))
        )
    }

  def initialModel(startupData: SnakeStartupData): Outcome[GameModel] =
    Outcome(GameModel.initialModel(startupData.viewConfig.gridSize, ControlScheme.directedKeys))

  def initialViewModel(startupData: SnakeStartupData, model: GameModel): Outcome[ViewModel] =
    Outcome(ViewModel.initialViewModel(startupData, model))

  def setup(viewConfig: ViewConfig, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[SnakeStartupData]] =
    SnakeStartupData.initialise(viewConfig)

  def updateModel(context: FrameContext[SnakeStartupData], model: GameModel): GlobalEvent => Outcome[GameModel] = {
    case GameReset =>
      Outcome(GameModel.initialModel(context.startUpData.viewConfig.gridSize, model.controlScheme))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      model: GameModel,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      model: GameModel,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

}
