package com.example.scalajsgame

import com.purplekingdomgames.indigo.gameengine._
import com.purplekingdomgames.indigo.renderer.ClearColor

object MyGame extends GameEngine[MyStartupData, MyErrorReport, Stuff] {

  private val viewportHeight: Int = 256
  private val viewportWidth: Int = 455

  def config: GameConfig = GameConfig(
    viewport = GameViewport(viewportWidth, viewportHeight),
    frameRate = 30,
    clearColor = ClearColor(0, 0, 0, 1),
    magnification = 1
  )

  def assets: Set[AssetType] = MyAssets.assets

  def initialise(assetCollection: AssetCollection): Startup[MyErrorReport, MyStartupData] = {
    val pass: Boolean = true

    if(pass) MyStartupData("Hello")
    else MyErrorReport(List("Boom!", "Boom!", "Shake the room!"))
  }

  def initialModel(startupData: MyStartupData): Stuff = MyModel.initialModel(startupData)

  def updateModel(gameTime: GameTime, state: Stuff): GameEvent => Stuff = MyModel.updateModel(assetCollection, gameTime, state)

  def updateView(currentState: Stuff): SceneGraphNode = MyView.updateView(currentState)

}

case class MyStartupData(name: String)

case class MyErrorReport(errors: List[String])

object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

}