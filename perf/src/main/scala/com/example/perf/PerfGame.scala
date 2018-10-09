package com.example.perf

import indigo._
import indigoexts.entrypoint._
import indigoexts.formats.Aseprite

import scala.scalajs.js.annotation.JSExportTopLevel

object PerfGame {

  val viewportWidth: Int      = 800
  val viewportHeight: Int     = 600
  val magnificationLevel: Int = 1

  def config: GameConfig =
    GameConfig(
      viewport = GameViewport(viewportWidth, viewportHeight),
      frameRate = 60,
      clearColor = ClearColor(0.4, 0.2, 0.5, 1),
      magnification = magnificationLevel,
      advanced = AdvancedGameConfig(
        recordMetrics = true,
        logMetricsReportIntervalMs = 5000,
        disableSkipModelUpdates = true,
        disableSkipViewUpdates = true
      )
    )

  def assets: Set[AssetType] =
    PerfAssets.assets

  def initialise(assetCollection: AssetCollection): Startup[MyErrorReport, MyStartupData] = {
    val dude: Option[Dude] = for {
      json                <- assetCollection.texts.find(p => p.name == PerfAssets.dudeName + "-json").map(_.contents)
      aseprite            <- Aseprite.fromJson(json)
      spriteAndAnimations <- Aseprite.toSpriteAndAnimations(aseprite, Depth(3), PerfAssets.dudeName)
      _                   <- Option(game.registerAnimations(spriteAndAnimations.animations))
    } yield
      Dude(
        aseprite,
        spriteAndAnimations.sprite
          .withRef(16, 16) // Initial offset, so when talk about his position it's the center of the sprite
          .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
      )

    dude match {
      case Some(d) => MyStartupData(d)
      case None    => MyErrorReport("Failed to load the dude")
    }
  }

  def initialModel(startupData: MyStartupData): MyGameModel =
    PerfModel.initialModel(startupData)

  val updateModel: (GameTime, MyGameModel) => GameEvent => MyGameModel = (_, gameModel) => PerfModel.updateModel(gameModel)

  val initialViewModel: (MyStartupData, MyGameModel) => MyViewModel = (_, _) => MyViewModel()

  val updateViewModel: (GameTime, MyGameModel, MyViewModel, FrameInputEvents) => MyViewModel = (_, _, previous, _) => previous

  val updateView: (GameTime, MyGameModel, MyViewModel, FrameInputEvents) => SceneUpdateFragment =
    (_, gameModel, _, frameInputEvents) => PerfView.updateView(gameModel, frameInputEvents)

  def game: IndigoGame[MyStartupData, MyErrorReport, MyGameModel, MyViewModel] =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .withFonts(Set(PerfView.fontInfo))
      .withAnimations(Set())
      .startUpGameWith(initialise)
      .usingInitialModel(initialModel)
      .updateModelUsing(updateModel)
      .initialiseViewModelUsing(initialViewModel)
      .updateViewModelUsing(updateViewModel)
      .presentUsing(updateView)

  @JSExportTopLevel("com.example.perf.PerfGame.main")
  def main(args: Array[String]): Unit =
    game.start()

}

case class Dude(aseprite: Aseprite, sprite: Sprite)
case class MyStartupData(dude: Dude)

case class MyErrorReport(errors: List[String])
object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): MyErrorReport = MyErrorReport(message.toList)

}

case class MyViewModel()
