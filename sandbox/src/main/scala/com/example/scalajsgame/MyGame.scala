package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Depth
import com.purplekingdomgames.indigo.renderer.ClearColor

object MyGame extends GameEngine[MyStartupData, MyErrorReport, MyGameModel] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = 455

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0, 0, 0, 1),
    magnification = 2
  )

  def assets: Set[AssetType] = MyAssets.assets

  def initialise(assetCollection: AssetCollection): Startup[MyErrorReport, MyStartupData] = {
    val dude = for {
      json <- assetCollection.texts.find(p => p.name == MyAssets.dudeName + "-json").map(_.contents)
      aseprite <- AsepriteHelper.fromJson(json)
      sprite <- AsepriteHelper.toSprite(aseprite, Depth(3), MyAssets.dudeName)
    } yield Dude(
      aseprite,
      sprite
        .withRef(16, 16) // Intial offset, so when talk about his position it's the center of the sprite
        .moveTo(viewportWidth / 2 / 2, viewportHeight / 2 / 2) // Also place him in the middle of the screen initially
    )

    dude match {
      case Some(d) => MyStartupData(d)
      case None => MyErrorReport("Failed to load the dude")
    }
  }

  def initialModel(startupData: MyStartupData): MyGameModel = MyModel.initialModel(startupData)

  def updateModel(gameTime: GameTime, state: MyGameModel): GameEvent => MyGameModel = MyModel.updateModel(assetCollection, gameTime, state)

  def updateView(currentState: MyGameModel): SceneGraphRootNode = MyView.updateView(currentState)

}

case class Dude(aseprite: Aseprite, sprite: Sprite)

case class MyStartupData(dude: Dude)

case class MyErrorReport(errors: List[String])

object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): MyErrorReport = MyErrorReport(message.toList)

}