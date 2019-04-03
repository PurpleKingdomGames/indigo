package indigoexts.entry

import indigo.gameengine.assets.AssetCollection
import indigo.gameengine.scenegraph.datatypes.FontInfo
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.gameengine.scenegraph.animation.Animation
import indigo.gameengine.subsystems.SubSystem
import indigo.gameengine.{events, _}
import indigo.shared.{AssetType, GameConfig}
import indigo.time.GameTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Indigo {
  def game: IndigoGameBase.type =
    IndigoGameBase
}

object IndigoGameBase {

  class IndigoGame[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GlobalEvent => Outcome[GameModel],
      initialViewModel: StartupData => GameModel => ViewModel,
      updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => Outcome[ViewModel],
      updateView: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => SceneUpdateFragment
  ) {

    private val gameEngine: GameEngine[StartupData, StartupError, GameModel, ViewModel] =
      GameEngine[StartupData, StartupError, GameModel, ViewModel](
        config,
        configAsync,
        assets,
        assetsAsync,
        fonts,
        animations,
        subSystems,
        initialise,
        initialModel,
        updateModel,
        initialViewModel,
        updateViewModel,
        updateView
      )

    def start(): Unit =
      gameEngine.start()
  }

  class IndigoGameWithViewModelUpdater[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GlobalEvent => Outcome[GameModel],
      initialViewModel: StartupData => GameModel => ViewModel,
      updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => Outcome[ViewModel]
  ) {
    def presentUsing(
        updateView: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => SceneUpdateFragment
    ): IndigoGame[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGame(config, configAsync, assets, assetsAsync, fonts, animations, subSystems, initialise, initialModel, updateModel, initialViewModel, updateViewModel, updateView)
  }

  class IndigoGameWithInitialViewModel[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GlobalEvent => Outcome[GameModel],
      initialViewModel: StartupData => GameModel => ViewModel
  ) {
    def updateViewModelUsing(
        updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => Outcome[ViewModel]
    ): IndigoGameWithViewModelUpdater[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGameWithViewModelUpdater(config, configAsync, assets, assetsAsync, fonts, animations, subSystems, initialise, initialModel, updateModel, initialViewModel, updateViewModel)
  }

  class IndigoGameWithModelUpdate[StartupData, StartupError, GameModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GlobalEvent => Outcome[GameModel]
  ) {
    def initialiseViewModelUsing[ViewModel](
        initialViewModel: (StartupData, GameModel) => ViewModel
    ): IndigoGameWithInitialViewModel[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGameWithInitialViewModel(
        config,
        configAsync,
        assets,
        assetsAsync,
        fonts,
        animations,
        subSystems,
        initialise,
        initialModel,
        updateModel,
        (sd: StartupData) => (gm: GameModel) => initialViewModel(sd, gm)
      )
  }

  class IndigoGameWithModel[StartupData, StartupError, GameModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel
  ) {
    def updateModelUsing(
        modelUpdater: (GameTime, GameModel) => events.GlobalEvent => Outcome[GameModel]
    ): IndigoGameWithModelUpdate[StartupData, StartupError, GameModel] =
      new IndigoGameWithModelUpdate(config, configAsync, assets, assetsAsync, fonts, animations, subSystems, initialise, initialModel, modelUpdater)
  }

  class InitialisedIndigoGame[StartupData, StartupError](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem],
      initialise: AssetCollection => Startup[StartupError, StartupData]
  ) {
    def usingInitialModel[GameModel](model: StartupData => GameModel): IndigoGameWithModel[StartupData, StartupError, GameModel] =
      new IndigoGameWithModel(config, configAsync, assets, assetsAsync, fonts, animations, subSystems, initialise, model)
  }

  class IndigoGameWithSubSystems(
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation],
      subSystems: Set[SubSystem]
  ) {
    def startUpGameWith[StartupData, StartupError](
        initializer: AssetCollection => Startup[StartupError, StartupData]
    ): InitialisedIndigoGame[StartupData, StartupError] =
      new InitialisedIndigoGame(config, configAsync, assets, assetsAsync, fonts, animations, subSystems, initializer)
  }

  class IndigoGameWithAnimations(
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animation]
  ) {
    def withSubSystems(subSystems: Set[SubSystem]): IndigoGameWithSubSystems =
      new IndigoGameWithSubSystems(config, configAsync, assets, assetsAsync, fonts, animations, subSystems)
    def noSubSystems: IndigoGameWithSubSystems =
      new IndigoGameWithSubSystems(config, configAsync, assets, assetsAsync, fonts, animations, Set())
  }

  class IndigoGameWithFonts(config: GameConfig, configAsync: Future[Option[GameConfig]], assets: Set[AssetType], assetsAsync: Future[Set[AssetType]], fonts: Set[FontInfo]) {
    def withAnimations(animations: Set[Animation]): IndigoGameWithAnimations =
      new IndigoGameWithAnimations(config, configAsync, assets, assetsAsync, fonts, animations)
    def noAnimations: IndigoGameWithAnimations =
      new IndigoGameWithAnimations(config, configAsync, assets, assetsAsync, fonts, Set())
  }

  class IndigoGameWithAssets(config: GameConfig, configAsync: Future[Option[GameConfig]], assets: Set[AssetType], assetsAsync: Future[Set[AssetType]]) {
    def withFonts(fonts: Set[FontInfo]): IndigoGameWithFonts =
      new IndigoGameWithFonts(config, configAsync, assets, assetsAsync, fonts)
    def noFonts: IndigoGameWithFonts =
      new IndigoGameWithFonts(config, configAsync, assets, assetsAsync, Set())
  }

  class ConfiguredIndigoGame(config: GameConfig, configAsync: Future[Option[GameConfig]]) {
    def withAssets(assets: Set[AssetType]): IndigoGameWithAssets =
      new IndigoGameWithAssets(config, configAsync, assets, Future(Set()))
    def withAsyncAssets(assetsAsync: Future[Set[AssetType]]): IndigoGameWithAssets =
      new IndigoGameWithAssets(config, configAsync, Set(), assetsAsync)
  }

  def withConfig(config: GameConfig): ConfiguredIndigoGame = new ConfiguredIndigoGame(config, Future(None))
  def withAsyncConfig(configAsync: Future[Option[GameConfig]]): ConfiguredIndigoGame =
    new ConfiguredIndigoGame(defaultGameConfig, configAsync)
}
