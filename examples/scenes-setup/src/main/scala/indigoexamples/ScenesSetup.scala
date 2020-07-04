package indigoexamples

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ScenesSetup extends IndigoGame[Unit, StartUpData, GameModel, Unit] {

  val targetFPS: Int = 30

  def boot(flags: Map[String, String]): BootResult[Unit] =
    BootResult
      .noData(
        defaultGameConfig
          .withClearColor(ClearColor.fromHexString("0xAA3399"))
          .withFrameRate(targetFPS)
      )
      .withAssets(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))
      .withFonts(FontStuff.fontInfo)
      .withSubSystems(FPSCounter(FontStuff.fontKey, Point(10, 360), targetFPS))

  def scenes(bootData: Unit): NonEmptyList[Scene[StartUpData, GameModel, Unit]] =
    NonEmptyList(SceneA, SceneB)

  def initialScene(bootData: Unit): Option[SceneName] = Option(SceneA.name)

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartUpData] =
    Startup.Success(StartUpData("Scene A!", "Scene B?"))

  def initialModel(startupData: StartUpData): GameModel =
    GameModel(
      sceneA = MessageA(startupData.messageA),
      sceneB = MessageB(startupData.messageB)
    )

  def initialViewModel(startupData: StartUpData, model: GameModel): Unit =
    ()
}

final case class StartUpData(messageA: String, messageB: String)
final case class GameModel(sceneA: MessageA, sceneB: MessageB)
