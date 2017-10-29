package com.example.sandbox

import com.purplekingdomgames.indigo.Indigo
import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.gameengine.assets.{AssetCollection, AssetType}
import com.purplekingdomgames.indigo.gameengine.scenegraph._
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Depth
import com.purplekingdomgames.indigo.renderer.ClearColor

import scala.scalajs.js.annotation.JSExportTopLevel

import com.purplekingdomgames.indigo._

object MyGame {

  private val viewportWidth: Int = 456
  private val viewportHeight: Int = 256
  private val magnificationLevel: Int = 2

  implicit val config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0.4, 0.2, 0.5, 1),
    magnification = magnificationLevel
  )

  implicit val assets: Set[AssetType] = MyAssets.assets

  implicit val initialise: AssetCollection => Startup[MyErrorReport, MyStartupData] = assetCollection => {
    val dude = for {
      json <- assetCollection.texts.find(p => p.name == MyAssets.dudeName + "-json").map(_.contents)
      aseprite <- AsepriteHelper.fromJson(json)
      sprite <- AsepriteHelper.toSprite[MyViewEventDataType](aseprite, Depth(3), MyAssets.dudeName)
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

  implicit val initialModel: MyStartupData => MyGameModel = startupData =>
    MyModel.initialModel(startupData)

  implicit val updateModel: (GameTime, MyGameModel) => GameEvent => MyGameModel = (_, gameModel) =>
    MyModel.updateModel(gameModel)

  implicit val updateView: (GameTime, MyGameModel, FrameInputEvents) => SceneGraphUpdate[MyViewEventDataType] = (_, gameModel, frameInputEvents) =>
    MyView.updateView(gameModel, frameInputEvents)

  @JSExportTopLevel("com.example.sandbox.MyGame.main")
  def main(args: Array[String]): Unit =
    Indigo.start[MyStartupData, MyErrorReport, MyGameModel, MyViewEventDataType]

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