package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.shared.{AssetType, GameConfig, GameDefinition}

import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Indigo")
object Framework {

  implicit val config: GameConfig =
    GameConfig.default

  implicit def configAsync: Future[Option[GameConfig]] =
    GameConfigHelper.load

  implicit val assets: Set[AssetType] =
    AssetsHelper.assets

  implicit def assetsAsync: Future[Set[AssetType]] =
    AssetsHelper.assetsAsync

  implicit val initialise: AssetCollection => Startup[StartupErrorReport, StartupData] = assetCollection =>
    assetCollection
      .texts
      .find(p => p.name == "indigoJson")
      .flatMap(json => GameDefinitionHelper.fromJson(json.contents)) match {
      case Some(gd) => StartupData(gd)
      case None => StartupErrorReport("Game definition could not be loaded")
    }

  implicit val initialModel: StartupData => GameModel = startupData =>
    GameModelHelper.initialModel(startupData)

  implicit val updateModel: (GameTime, GameModel) => GameEvent => GameModel = (_, gameModel) =>
    GameModelHelper.updateModel(gameModel)

  implicit val updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate[GameViewEvent] = (_, gameModel, _) =>
    GameViewHelper.updateView(gameModel)

  @JSExport
  def startLocal(): Unit =
    Indigo.start[StartupData, StartupErrorReport, GameModel, GameViewEvent]

  @JSExport
  def startRemote(): Unit =
    Indigo.start[StartupData, StartupErrorReport, GameModel, GameViewEvent]

}

case class StartupErrorReport(message: String)
object StartupErrorReport {
  implicit val toErrorReport: ToReportable[StartupErrorReport] =
    ToReportable.createToReportable(r => r.message)
}

case class StartupData(gameDefinition: GameDefinition)
