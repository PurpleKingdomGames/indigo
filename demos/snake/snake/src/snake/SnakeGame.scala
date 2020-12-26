package snake

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import snake.model.{ControlScheme, SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings, SnakeStartupData}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[GameViewport, SnakeStartupData, SnakeGameModel, SnakeViewModel] {

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: GameViewport): NonEmptyList[Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel]] =
    NonEmptyList(StartScene, ControlsScene, GameScene, GameOverScene)

  val eventFilters: EventFilters =
    EventFilters.BlockAll

  def boot(flags: Map[String, String]): Outcome[BootResult[GameViewport]] =
    Outcome {
      val assetPath: String =
        flags.getOrElse("baseUrl", "")

      val config =
        GameConfig(
          viewport = GameViewport(Settings.viewportWidth, Settings.viewportHeight),
          frameRate = 60,
          clearColor = RGBA.Black,
          magnification = Settings.magnificationLevel
        )

      BootResult(config, config.viewport)
        .withAssets(GameAssets.assets(assetPath))
        .withFonts(GameAssets.fontInfo)
        .withSubSystems(
          Set(FPSCounter(GameAssets.fontKey, Point(5, 5), 60))
        )
    }

  def initialModel(startupData: SnakeStartupData): Outcome[SnakeGameModel] =
    Outcome(SnakeGameModel.initialModel(startupData, ControlScheme.directedKeys))

  def initialViewModel(startupData: SnakeStartupData, model: SnakeGameModel): Outcome[SnakeViewModel] =
    Outcome(SnakeViewModel.initialViewModel(startupData, model))

  def setup(viewport: GameViewport, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[SnakeStartupData]] =
    SnakeStartupData.initialise(viewport, Settings.gridSize)

  def updateModel(context: FrameContext[SnakeStartupData], model: SnakeGameModel): GlobalEvent => Outcome[SnakeGameModel] =
    _ => Outcome(model)

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
