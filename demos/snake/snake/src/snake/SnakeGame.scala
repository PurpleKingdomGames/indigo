package snake

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import snake.model.{ControlScheme, SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, SnakeStartupData, ViewConfig}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[ViewConfig, SnakeStartupData, SnakeGameModel, SnakeViewModel] {

  def initialScene(bootData: ViewConfig): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: ViewConfig): NonEmptyList[Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel]] =
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

  def initialModel(startupData: SnakeStartupData): Outcome[SnakeGameModel] =
    Outcome(SnakeGameModel.initialModel(startupData.viewConfig.gridSize, ControlScheme.directedKeys))

  def initialViewModel(startupData: SnakeStartupData, model: SnakeGameModel): Outcome[SnakeViewModel] =
    Outcome(SnakeViewModel.initialViewModel(startupData, model))

  def setup(viewConfig: ViewConfig, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[SnakeStartupData]] =
    SnakeStartupData.initialise(viewConfig)

  def updateModel(context: FrameContext[SnakeStartupData], model: SnakeGameModel): GlobalEvent => Outcome[SnakeGameModel] = {
    case GameReset =>
      Outcome(model.reset)

    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[SnakeStartupData],
      model: SnakeGameModel,
      viewModel: SnakeViewModel
  ): GlobalEvent => Outcome[SnakeViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[SnakeStartupData],
      model: SnakeGameModel,
      viewModel: SnakeViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

}
