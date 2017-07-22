package com.purplekingdomgames.indigoframework

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.{AssetCollection, AssetType}
import com.purplekingdomgames.indigo.gameengine.scenegraph._

import scala.concurrent.Future

object Framework extends GameEngine[StartupData, StartupErrorReport, GameModel, GameViewEvent] {

//  private val viewportWidth: Int = 456
//  private val viewportHeight: Int = 256
//  private val magnificationLevel: Int = 2

  def config: GameConfig = GameConfig.default

  override def configAsync: Future[Option[GameConfig]] = GameConfigHelper.load

  def assets: Set[AssetType] = AssetsHelper.assets
  override def assetsAsync: Future[Set[AssetType]] = AssetsHelper.assetsAsync

  def initialise(assetCollection: AssetCollection): Startup[StartupErrorReport, StartupData] = {

    val gameDef: Option[GameDefinition] = assetCollection.texts.find(p => p.name == "indigoJson").flatMap(json => GameDefinitionHelper.fromJson(json.contents))

//    val dude = for {
//      json <- assetCollection.texts.find(p => p.name == "base_charactor-json").map(_.contents)
//      aseprite <- AsepriteHelper.fromJson(json)
//      sprite <- AsepriteHelper.toSprite(aseprite, Depth(3), "base_charactor")
//    } yield Dude(
//      aseprite,
//      sprite
//        .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
//        .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
//    )
//
//    dude match {
//      case Some(d) => MyStartupData(d)
//      case None => MyErrorReport("Failed to load the dude")
//    }

    gameDef match {
      case Some(gd) => StartupData(gd)
      case None => StartupErrorReport("Game definition could not be loaded")
    }

  }

  def initialModel(startupData: StartupData): GameModel = GameModelHelper.initialModel(startupData)

  def updateModel(gameTime: GameTime, gameModel: GameModel): GameEvent => GameModel = GameModelHelper.updateModel(gameTime, gameModel)

  def updateView(gameTime: GameTime, gameModel: GameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[GameViewEvent] =
    GameViewHelper.updateView(gameTime, gameModel, frameInputEvents)

}


case class StartupErrorReport(message: String)
object StartupErrorReport {

  implicit val toErrorReport: ToReportable[StartupErrorReport] =
    ToReportable.createToReportable(r => r.message)

}
case class StartupData(gameDefinition: GameDefinition)

//case class Dude(aseprite: Aseprite, sprite: Sprite[MyViewEventDataType])
//case class MyStartupData(dude: Dude)
//
//case class MyErrorReport(errors: List[String])
//object MyErrorReport {
//
//  implicit val toErrorReport: ToReportable[MyErrorReport] =
//    ToReportable.createToReportable(r => r.errors.mkString("\n"))
//
//  def apply(message: String*): MyErrorReport = MyErrorReport(message.toList)
//
//}
//
//case class MyViewEventDataType()
