package com.example.perf

import indigo._
import indigo.shared.EqualTo._
import indigoexts.entrypoint._
import indigoexts.formats._

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
    def makeStartupData(aseprite: Aseprite, spriteAndAnimations: SpriteAndAnimations): Startup.Success[MyStartupData] =
      Startup
        .Success(
          MyStartupData(
            Dude(
              aseprite,
              spriteAndAnimations.sprite
                .withRef(16, 16)                                                                         // Initial offset, so when talk about his position it's the center of the sprite
                .moveTo(viewportWidth / 2 / magnificationLevel, viewportHeight / 2 / magnificationLevel) // Also place him in the middle of the screen initially
            )
          )
        )
        .addAnimations(spriteAndAnimations.animations)

    val res: Option[Startup.Success[MyStartupData]] = for {
      json                <- assetCollection.texts.find(p => p.name === PerfAssets.dudeName + "-json").map(_.contents)
      aseprite            <- Aseprite.fromJson(json)
      spriteAndAnimations <- Aseprite.toSpriteAndAnimations(aseprite, Depth(3), PerfAssets.dudeName)
    } yield makeStartupData(aseprite, spriteAndAnimations)

    res.getOrElse(Startup.Failure(MyErrorReport("Failed to load the dude")))
  }

  def initialModel(startupData: MyStartupData): MyGameModel =
    PerfModel.initialModel(startupData)

  val updateModel: (GameTime, MyGameModel) => GlobalEvent => Outcome[MyGameModel] = (_, gameModel) => PerfModel.updateModel(gameModel)

  val initialViewModel: (MyStartupData, MyGameModel) => FpsCounter = (_, _) => FpsCounter.empty

  val updateViewModel: (GameTime, MyGameModel, FpsCounter, FrameInputEvents) => Outcome[FpsCounter] = (gameTime, _, previous, _) => Outcome(FpsCounter.update(gameTime, previous))

  val updateView: (GameTime, MyGameModel, FpsCounter, FrameInputEvents) => SceneUpdateFragment =
    (_, gameModel, fpsCounter, frameInputEvents) => PerfView.updateView(gameModel, fpsCounter, frameInputEvents)

  def game: IndigoGame[MyStartupData, MyErrorReport, MyGameModel, FpsCounter] =
    Indigo.game
      .withConfig(config)
      .withAssets(assets)
      .withFonts(Set(PerfView.fontInfo))
      .noAnimations
      .noSubSystems
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

final case class Dude(aseprite: Aseprite, sprite: Sprite)
final case class MyStartupData(dude: Dude)

final case class MyErrorReport(errors: List[String])
object MyErrorReport {

  implicit val toErrorReport: ToReportable[MyErrorReport] =
    ToReportable.createToReportable(r => r.errors.mkString("\n"))

  def apply(message: String*): MyErrorReport = MyErrorReport(message.toList)

}
