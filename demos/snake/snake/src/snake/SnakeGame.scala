package snake

import indigo._
import indigogame._
import indigoexts.scenes._
// import indigoexts.subsystems.fpscounter.FPSCounter

import snake.model.{ControlScheme, SnakeGameModel, SnakeViewModel}
import snake.init.{GameAssets, Settings, SnakeStartupData}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[SnakeStartupData, SnakeGameModel, SnakeViewModel] {

  val config: GameConfig =
    GameConfig(
      viewport = GameViewport(Settings.viewportWidth, Settings.viewportHeight),
      frameRate = 30,
      clearColor = ClearColor.Black,
      magnification = Settings.magnificationLevel
    )

  val animations: Set[Animation] =
    Set()

  val assets: Set[AssetType] =
    GameAssets.assets

  val fonts: Set[FontInfo] =
    Set(GameAssets.fontInfo)

  val initialScene: Option[SceneName] =
    Option(StartScene.name)

  val subSystems: Set[SubSystem] =
    Set()
    // Set(FPSCounter.subSystem(GameAssets.fontKey, Point(5, 5), 30))

  val scenes: NonEmptyList[Scene[SnakeGameModel, SnakeViewModel]] =
    NonEmptyList(StartScene, ControlsScene, GameScene, GameOverScene)

  def initialModel(startupData: SnakeStartupData): SnakeGameModel =
    SnakeGameModel.initialModel(startupData, ControlScheme.directedKeys)

  def initialViewModel(startupData: SnakeStartupData): SnakeGameModel => SnakeViewModel =
    m => SnakeViewModel.initialViewModel(startupData, m)

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, SnakeStartupData] =
    SnakeStartupData.initialise(config.viewport, Settings.gridSize)

}
