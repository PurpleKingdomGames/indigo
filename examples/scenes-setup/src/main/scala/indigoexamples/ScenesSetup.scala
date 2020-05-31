package indigoexamples

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ScenesSetup extends IndigoGame[Unit, StartUpData, GameModel, Unit] {

  def parseFlags(flags: Map[String, String]): Unit = ()

  val targetFPS: Int = 30

  def scenes(flagData: Unit): NonEmptyList[Scene[GameModel, Unit]] =
    NonEmptyList(SceneA, SceneB)

  def initialScene(flagData: Unit): Option[SceneName] = Option(SceneA.name)

  def config(flagData: Unit): GameConfig =
    defaultGameConfig
      .withClearColor(ClearColor.fromHexString("0xAA3399"))
      .withFrameRate(targetFPS)

  def assets(flagData: Unit): Set[AssetType] = Set(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set(FPSCounter.subSystem(FontStuff.fontKey, Point(10, 360), targetFPS))

  def setup(flagData: Unit, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartUpData] =
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
