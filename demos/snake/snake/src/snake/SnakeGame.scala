package snake

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.FPSCounter

import snake.model.{ControlScheme, GameModel, ViewModel}
import snake.init.{GameAssets, StartupData, ViewConfig}
import snake.scenes.{ControlsScene, GameOverScene, GameScene, StartScene}
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object SnakeGame extends IndigoGame[ViewConfig, StartupData, GameModel, ViewModel]:

  def initialScene(bootData: ViewConfig): Option[SceneName] =
    Option(StartScene.name)

  def scenes(bootData: ViewConfig): NonEmptyList[Scene[StartupData, GameModel, ViewModel]] =
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
          clearColor = RGBA.Black,
          magnification = viewConfig.magnificationLevel
        ).withFrameRateLimit(60)

      BootResult(config, viewConfig)
        .withAssets(GameAssets.assets(assetPath))
        .withFonts(GameAssets.fontInfo)
        .withSubSystems(
          Set(FPSCounter(Point(5, 5), BindingKey("fps")))
        )
    }

  def initialModel(startupData: StartupData): Outcome[GameModel] =
    Outcome(GameModel.initialModel(startupData.viewConfig.gridSize, ControlScheme.directedKeys))

  def initialViewModel(startupData: StartupData, model: GameModel): Outcome[ViewModel] =
    Outcome(ViewModel.initialViewModel(startupData, model))

  def setup(viewConfig: ViewConfig, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartupData]] =
    StartupData.initialise(viewConfig)

  def updateModel(context: FrameContext[StartupData], model: GameModel): GlobalEvent => Outcome[GameModel] = {
    case GameReset =>
      Outcome(GameModel.initialModel(context.startUpData.viewConfig.gridSize, model.controlScheme))

    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[StartupData],
      model: GameModel,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[StartupData],
      model: GameModel,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addLayer(Layer(BindingKey("game")))
        .addLayer(Layer(BindingKey("score")))
        .addLayer(Layer(BindingKey("ui")))
        .addLayer(Layer(BindingKey("fps")))
    )

case object GameReset extends GlobalEvent
