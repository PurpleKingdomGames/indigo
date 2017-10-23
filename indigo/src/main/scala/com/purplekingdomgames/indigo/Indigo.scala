package com.purplekingdomgames.indigo

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.{AssetCollection, AssetType}
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneGraphUpdate

import scala.concurrent.Future

object Indigo {

  def start[StartupData, StartupError, GameModel, ViewEventDataType](implicit
                                                                     config: GameConfig,
                                                                     configAsync: Future[Option[GameConfig]],
                                                                     assets: Set[AssetType],
                                                                     assetsAsync: Future[Set[AssetType]],
                                                                     initialise: AssetCollection => Startup[StartupError, StartupData],
                                                                     initialModel: StartupData => GameModel,
                                                                     updateModel: (GameTime, GameModel) => GameEvent => GameModel,
                                                                     updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate[ViewEventDataType]): Unit =
    new GameEngine[StartupData, StartupError, GameModel, ViewEventDataType](config, configAsync, assets, assetsAsync, initialise, initialModel, updateModel, updateView).start()

}
