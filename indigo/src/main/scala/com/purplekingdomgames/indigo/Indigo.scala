package com.purplekingdomgames.indigo

import com.purplekingdomgames.indigo.gameengine.assets.{AssetCollection, AssetType}
import com.purplekingdomgames.indigo.gameengine.scenegraph.{SceneGraphRootNode, SceneGraphUpdate}
import com.purplekingdomgames.indigo.gameengine._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Indigo {

  def start[StartupData, StartupError, GameModel, ViewEventDataType](implicit indigoGameRequirements: IndigoGameRequirements[StartupData, StartupError, GameModel, ViewEventDataType]): Unit =
    new GameEngine[StartupData, StartupError, GameModel, ViewEventDataType](indigoGameRequirements).start()
  /*
config: GameConfig,
configAsync: Future[Option[GameConfig]],
assets: Set[AssetType],
assetsAsync: Future[Set[AssetType]],
initialise: AssetCollection => Startup[StartupError, StartupData],
initialModel: StartupData => GameModel,
updateModel: (GameTime, GameModel) => GameEvent => GameModel,
updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate[ViewEventDataType]
   */

}

case class IndigoGameRequirements[StartupData, StartupError, GameModel, ViewEventDataType](
                                                                                    config: GameConfig,
                                                                                    configAsync: Future[Option[GameConfig]],
                                                                                    assets: Set[AssetType],
                                                                                    assetsAsync: Future[Set[AssetType]],
                                                                                    initialise: AssetCollection => Startup[StartupError, StartupData],
                                                                                    initialModel: StartupData => GameModel,
                                                                                    updateModel: (GameTime, GameModel) => GameEvent => GameModel,
                                                                                    updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate[ViewEventDataType]
                                                                                  )

object IndigoGameRequirements {

  implicit val blank: IndigoGameRequirements[Unit, Unit, Unit, Unit] =
    IndigoGameRequirements(
      GameConfig.default,
      Future(None),
      Set(),
      Future(Set()),
      _ => (),
      _ => (),
      (_, _) => _ => (),
      (_, _, _) => SceneGraphUpdate(SceneGraphRootNode.empty, Nil)
    )

}