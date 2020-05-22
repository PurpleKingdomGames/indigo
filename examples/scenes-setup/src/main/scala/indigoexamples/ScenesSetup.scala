package indigoexamples

import indigo._
import indigogame._
import indigogame.scenemanager._
import indigoextras.subsystems.fpscounter.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ScenesSetup extends IndigoGame[StartUpData, GameModel, Unit] {

  val targetFPS: Int = 30

  val scenes: NonEmptyList[Scene[GameModel, Unit]] =
    NonEmptyList(SceneA, SceneB)

  val initialScene: Option[SceneName] = Option(SceneA.name)

  val config: GameConfig =
    defaultGameConfig
      .withClearColor(ClearColor.fromHexString("0xAA3399"))
      .withFrameRate(targetFPS)

  val assets: Set[AssetType] = Set(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set(FPSCounter.subSystem(FontStuff.fontKey, Point(10, 360), targetFPS))

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, StartUpData] =
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
