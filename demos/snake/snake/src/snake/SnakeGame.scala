package snake

import indigo._
import indigo.scenes._
// import indigoextras.subsystems.FPSCounter

import snake.model.{ControlScheme, SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings, SnakeStartupData}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[GameViewport, SnakeStartupData, SnakeGameModel, SnakeViewModel] {

  def boot(flags: Map[String, String]): BootResult[GameViewport] = {
    val assetPath: String =
      flags.getOrElse("baseUrl", "")

    val config =
      GameConfig(
        viewport = GameViewport(Settings.viewportWidth, Settings.viewportHeight),
        frameRate = 30,
        clearColor = ClearColor.Black,
        magnification = Settings.magnificationLevel
      )

    BootResult(config, config.viewport)
      .withAssets(GameAssets.assets(assetPath))
      .withFonts(GameAssets.fontInfo)
      .withSubSystems(
        // Set(FPSCounter.subSystem(GameAssets.fontKey, Point(5, 5), 30))
      )
  }

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: GameViewport): NonEmptyList[Scene[SnakeStartupData, SnakeGameModel, SnakeViewModel]] =
    NonEmptyList(StartScene, ControlsScene, GameScene, GameOverScene)

  def initialModel(startupData: SnakeStartupData): SnakeGameModel =
    SnakeGameModel.initialModel(startupData, ControlScheme.directedKeys)

  def initialViewModel(startupData: SnakeStartupData, model: SnakeGameModel): SnakeViewModel =
    SnakeViewModel.initialViewModel(startupData, model)

  def setup(viewport: GameViewport, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, SnakeStartupData] =
    SnakeStartupData.initialise(viewport, Settings.gridSize)

}
