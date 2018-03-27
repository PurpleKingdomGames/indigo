package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.AssetCollection
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.shared.{AssetType, GameConfig, GameDefinition}

import scala.concurrent.Future
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Indigo")
object Framework {

  val config: GameConfig =
    GameConfig.default

  def configAsync: Future[Option[GameConfig]] =
    GameConfigHelper.load

  val assets: Set[AssetType] =
    AssetsHelper.assets

  def assetsAsync: Future[Set[AssetType]] =
    AssetsHelper.assetsAsync

  val initialise: AssetCollection => Startup[StartupErrorReport, StartupData] = assetCollection =>
    assetCollection
      .texts
      .find(p => p.name == "indigoJson")
      .flatMap(json => GameDefinitionHelper.fromJson(json.contents)) match {
      case Some(gd) => StartupData(gd)
      case None => StartupErrorReport("Game definition could not be loaded")
    }

  val initialModel: StartupData => GameModel = startupData =>
    GameModelHelper.initialModel(startupData)

  val updateModel: (GameTime, GameModel) => GameEvent => GameModel = (_, gameModel) =>
    GameModelHelper.updateModel(gameModel)

  val updateView: (GameTime, GameModel, FrameInputEvents) => SceneGraphUpdate = (_, gameModel, _) =>
    GameViewHelper.updateView(gameModel)

  @JSExport
  def startLocal(): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .startUpGameWith(initialise)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .presentUsing(updateView)
      .start()

  @JSExport
  def startRemote(): Unit =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .startUpGameWith(initialise)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .presentUsing(updateView)
      .start()

}

case class StartupErrorReport(message: String)
object StartupErrorReport {
  implicit val toErrorReport: ToReportable[StartupErrorReport] =
    ToReportable.createToReportable(r => r.message)
}

case class StartupData(gameDefinition: GameDefinition)
