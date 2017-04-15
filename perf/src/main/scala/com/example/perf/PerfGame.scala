package com.example.perf

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.{AssetCollection, AssetType}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Depth
import com.purplekingdomgames.indigo.renderer.ClearColor

object PerfGame extends GameEngine[MyStartupData, MyErrorReport, MyGameModel, MyViewEventDataType] {

  val viewportWidth: Int = 1024
  val viewportHeight: Int = 768
  val magnificationLevel: Int = 1

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0.4, 0.2, 0.5, 1),
    magnification = magnificationLevel,
    advanced = AdvancedGameConfig(
      recordMetrics = true,
      logMetricsReportIntervalMs = 5000,
      disableSkipModelUpdates = true,
      disableSkipViewUpdates = true
    )
  )

  def assets: Set[AssetType] = PerfAssets.assets

  def initialise(assetCollection: AssetCollection): Startup[MyErrorReport, MyStartupData] = {
    val dude = for {
      json <- assetCollection.texts.find(p => p.name == PerfAssets.dudeName + "-json").map(_.contents)
      aseprite <- AsepriteHelper.fromJson(json)
      sprite <- AsepriteHelper.toSprite(aseprite, Depth(3), PerfAssets.dudeName)
    } yield Dude(
      aseprite,
      sprite
        .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
        .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
    )

    dude match {
      case Some(d) => MyStartupData(d)
      case None => MyErrorReport("Failed to load the dude")
    }
  }

  def initialModel(startupData: MyStartupData): MyGameModel = PerfModel.initialModel(startupData)

  def updateModel(gameTime: GameTime, gameModel: MyGameModel): GameEvent => MyGameModel = PerfModel.updateModel(gameTime, gameModel)

  def updateView(gameTime: GameTime, gameModel: MyGameModel, frameInputEvents: FrameInputEvents): SceneGraphUpdate[MyViewEventDataType] =
    PerfView.updateView(gameTime, gameModel, frameInputEvents)

}

case class Dude(aseprite: Aseprite, sprite: Sprite[MyViewEventDataType])
case class MyStartupData(dude: Dude)

case class MyErrorReport(errors: List[String])
object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): MyErrorReport = MyErrorReport(message.toList)

}

case class MyViewEventDataType()