package com.purplekingdomgames.indigoexts.entry

import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.FontInfo
import com.purplekingdomgames.indigo.gameengine.scenegraph.{Animations, SceneUpdateFragment}
import com.purplekingdomgames.indigo.gameengine.{events, _}
import com.purplekingdomgames.shared.{AssetType, GameConfig}

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
      animations: Set[Animations],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GameEvent => GameModel,
      initialViewModel: GameModel => ViewModel,
      updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => ViewModel,
      updateView: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => SceneUpdateFragment
  ) {

    private val gameEngine: GameEngine[StartupData, StartupError, GameModel, ViewModel] =
      new GameEngine[StartupData, StartupError, GameModel, ViewModel](config,
                                                                      configAsync,
                                                                      assets,
                                                                      assetsAsync,
                                                                      fonts,
                                                                      animations,
                                                                      initialise,
                                                                      initialModel,
                                                                      updateModel,
                                                                      initialViewModel,
                                                                      updateViewModel,
                                                                      updateView)

    def registerAnimations(animations: Animations): Unit =
      gameEngine.registerAnimations(animations)

    def registerFont(fontInfo: FontInfo): Unit =
      gameEngine.registerFont(fontInfo)

    def start(): Unit =
      gameEngine.start()
  }

  class IndigoGameWithViewModelUpdater[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animations],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GameEvent => GameModel,
      initialViewModel: GameModel => ViewModel,
      updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => ViewModel
  ) {
    def presentUsing(
        updateView: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => SceneUpdateFragment
    ): IndigoGame[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGame(config,
                     configAsync,
                     assets,
                     assetsAsync,
                     fonts,
                     animations,
                     initialise,
                     initialModel,
                     updateModel,
                     initialViewModel,
                     updateViewModel,
                     updateView)
  }

  class IndigoGameWithInitialViewModel[StartupData, StartupError, GameModel, ViewModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animations],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GameEvent => GameModel,
      initialViewModel: GameModel => ViewModel
  ) {
    def updateViewModelUsing(
        updateViewModel: (GameTime, GameModel, ViewModel, events.FrameInputEvents) => ViewModel
    ): IndigoGameWithViewModelUpdater[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGameWithViewModelUpdater(config,
                                         configAsync,
                                         assets,
                                         assetsAsync,
                                         fonts,
                                         animations,
                                         initialise,
                                         initialModel,
                                         updateModel,
                                         initialViewModel,
                                         updateViewModel)
  }

  class IndigoGameWithModelUpdate[StartupData, StartupError, GameModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animations],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel,
      updateModel: (GameTime, GameModel) => events.GameEvent => GameModel
  ) {
    def initialiseViewModelUsing[ViewModel](
        initialViewModel: GameModel => ViewModel
    ): IndigoGameWithInitialViewModel[StartupData, StartupError, GameModel, ViewModel] =
      new IndigoGameWithInitialViewModel(
        config,
        configAsync,
        assets,
        assetsAsync,
        fonts,
        animations,
        initialise,
        initialModel,
        updateModel,
        initialViewModel
      )
  }

  class IndigoGameWithModel[StartupData, StartupError, GameModel](
      config: GameConfig,
      configAsync: Future[Option[GameConfig]],
      assets: Set[AssetType],
      assetsAsync: Future[Set[AssetType]],
      fonts: Set[FontInfo],
      animations: Set[Animations],
      initialise: AssetCollection => Startup[StartupError, StartupData],
      initialModel: StartupData => GameModel
  ) {
    def updateModelUsing(
        modelUpdater: (GameTime, GameModel) => events.GameEvent => GameModel
    ): IndigoGameWithModelUpdate[StartupData, StartupError, GameModel] =
      new IndigoGameWithModelUpdate(config,
                                    configAsync,
                                    assets,
                                    assetsAsync,
                                    fonts,
                                    animations,
                                    initialise,
                                    initialModel,
                                    modelUpdater)
  }

  class InitialisedIndigoGame[StartupData, StartupError](config: GameConfig,
                                                         configAsync: Future[Option[GameConfig]],
                                                         assets: Set[AssetType],
                                                         assetsAsync: Future[Set[AssetType]],
                                                         fonts: Set[FontInfo],
                                                         animations: Set[Animations],
                                                         initialise: AssetCollection => Startup[StartupError, StartupData]) {
    def usingInitialModel[GameModel](model: StartupData => GameModel): IndigoGameWithModel[StartupData, StartupError, GameModel] =
      new IndigoGameWithModel(config, configAsync, assets, assetsAsync, fonts, animations, initialise, model)
  }

  class IndigoGameWithAnimations(config: GameConfig,
                                 configAsync: Future[Option[GameConfig]],
                                 assets: Set[AssetType],
                                 assetsAsync: Future[Set[AssetType]],
                                 fonts: Set[FontInfo],
                                 animations: Set[Animations]) {
    def startUpGameWith[StartupData, StartupError](
        initializer: AssetCollection => Startup[StartupError, StartupData]
    ): InitialisedIndigoGame[StartupData, StartupError] =
      new InitialisedIndigoGame(config, configAsync, assets, assetsAsync, fonts, animations, initializer)
  }

  class IndigoGameWithFonts(config: GameConfig,
                            configAsync: Future[Option[GameConfig]],
                            assets: Set[AssetType],
                            assetsAsync: Future[Set[AssetType]],
                            fonts: Set[FontInfo]) {
    def withAnimations(animations: Set[Animations]): IndigoGameWithAnimations =
      new IndigoGameWithAnimations(config, configAsync, assets, assetsAsync, fonts, animations)
  }

  class IndigoGameWithAssets(config: GameConfig,
                             configAsync: Future[Option[GameConfig]],
                             assets: Set[AssetType],
                             assetsAsync: Future[Set[AssetType]]) {
    def withFonts(fonts: Set[FontInfo]): IndigoGameWithFonts =
      new IndigoGameWithFonts(config, configAsync, assets, assetsAsync, fonts)
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
