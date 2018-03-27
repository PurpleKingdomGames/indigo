package com.purplekingdomgames.indigo

import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphUpdate
import com.purplekingdomgames.indigo.gameengine.{events, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Indigo {
  def game: IndigoGameBase.type =
    IndigoGameBase
}

object IndigoGameBase {

  class IndigoGame[StartupData, StartupError, GameModel](config: GameConfig,
                                                         configAsync: Future[Option[GameConfig]],
                                                         assets: Set[AssetType],
                                                         assetsAsync: Future[Set[AssetType]],
                                                         initialise: AssetCollection => Startup[StartupError, StartupData],
                                                         initialModel: StartupData => GameModel,
                                                         updateModel: (GameTime, GameModel) => events.GameEvent => GameModel,
                                                         updateView: (GameTime, GameModel, events.FrameInputEvents) => SceneGraphUpdate) {
    def start(): Unit =
      new GameEngine[StartupData, StartupError, GameModel](config, configAsync, assets, assetsAsync, initialise, initialModel, updateModel, updateView).start()
  }

  class IndigoGameWithModelUpdate[StartupData, StartupError, GameModel](config: GameConfig,
                                                                        configAsync: Future[Option[GameConfig]],
                                                                        assets: Set[AssetType],
                                                                        assetsAsync: Future[Set[AssetType]],
                                                                        initialise: AssetCollection => Startup[StartupError, StartupData],
                                                                        initialModel: StartupData => GameModel,
                                                                        updateModel: (GameTime, GameModel) => events.GameEvent => GameModel) {
    def presentUsing(updateView: (GameTime, GameModel, events.FrameInputEvents) => SceneGraphUpdate): IndigoGame[StartupData, StartupError, GameModel] =
      new IndigoGame(
        config: GameConfig,
        configAsync: Future[Option[GameConfig]],
        assets: Set[AssetType],
        assetsAsync: Future[Set[AssetType]],
        initialise: AssetCollection => Startup[StartupError, StartupData],
        initialModel: StartupData => GameModel,
        updateModel: (GameTime, GameModel) => events.GameEvent => GameModel,
        updateView: (GameTime, GameModel, events.FrameInputEvents) => SceneGraphUpdate
      )
  }

  class IndigoGameWithModel[StartupData, StartupError, GameModel](config: GameConfig,
                                                                  configAsync: Future[Option[GameConfig]],
                                                                  assets: Set[AssetType],
                                                                  assetsAsync: Future[Set[AssetType]],
                                                                  initialise: AssetCollection => Startup[StartupError, StartupData],
                                                                  initialModel: StartupData => GameModel) {
    def updateModelUsing(modelUpdater: (GameTime, GameModel) => events.GameEvent => GameModel): IndigoGameWithModelUpdate[StartupData, StartupError, GameModel] =
      new IndigoGameWithModelUpdate(config, configAsync, assets, assetsAsync, initialise, initialModel, modelUpdater)
  }

  class InitialisedIndigoGame[StartupData, StartupError](config: GameConfig,
                                                         configAsync: Future[Option[GameConfig]],
                                                         assets: Set[AssetType],
                                                         assetsAsync: Future[Set[AssetType]],
                                                         initialise: AssetCollection => Startup[StartupError, StartupData]) {
    def usingInitialModel[GameModel](model: StartupData => GameModel): IndigoGameWithModel[StartupData, StartupError, GameModel] =
      new IndigoGameWithModel(config, configAsync, assets, assetsAsync, initialise, model)
  }

  class IndigoGameWithAssets(config: GameConfig,
                             configAsync: Future[Option[GameConfig]],
                             assets: Set[AssetType],
                             assetsAsync: Future[Set[AssetType]]) {
    def startUpGameWith[StartupData, StartupError](initializer: AssetCollection => Startup[StartupError, StartupData]) =
      new InitialisedIndigoGame(config, configAsync, assets, assetsAsync, initializer)
  }

  class ConfiguredIndigoGame(config: GameConfig, configAsync: Future[Option[GameConfig]]) {
    def withAssets(assets: Set[AssetType]): IndigoGameWithAssets = new IndigoGameWithAssets(config, configAsync, assets, Future(Set()))
    def withAsyncAssets(assetsAsync: Future[Set[AssetType]]): IndigoGameWithAssets = new IndigoGameWithAssets(config, configAsync, Set(), assetsAsync)
  }

  def withConfig(config: GameConfig): ConfiguredIndigoGame = new ConfiguredIndigoGame(config, Future(None))
  def withAsyncConfig(configAsync: Future[Option[GameConfig]]): ConfiguredIndigoGame = new ConfiguredIndigoGame(defaultGameConfig, configAsync)
}

