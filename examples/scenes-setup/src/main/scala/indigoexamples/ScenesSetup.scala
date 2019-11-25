package indigoexamples

import indigo._
import indigoexts.entrypoint._
import indigoexts.scenes._
import indigoexts.subsystems.fpscounter.FPSCounter

object ScenesSetup extends IndigoGameWithScenes[StartUpData, GameModel, Unit] {

  val scenes: NonEmptyList[Scene[GameModel, Unit]] =
    NonEmptyList(SceneA, SceneB)

  val initialScene: Option[SceneName] = Option(SceneA.name)

  val config: GameConfig = defaultGameConfig.withClearColor(ClearColor.fromHexString("0xAA3399"))

  val assets: Set[AssetType] = Set(AssetType.Image(FontStuff.fontName, "assets/boxy_font.png"))

  val fonts: Set[FontInfo] = Set(FontStuff.fontInfo)

  val animations: Set[Animation] = Set()

  val subSystems: Set[SubSystem] = Set(FPSCounter.subSystem(FontStuff.fontKey, Point(10, 360)))

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartUpData] =
    Startup.Success(StartUpData("Scene A!", "Scene B?"))

  def initialModel(startupData: StartUpData): GameModel =
    GameModel(
      sceneA = MessageA(startupData.messageA),
      sceneB = MessageB(startupData.messageB)
    )

  def initialViewModel(startupData: StartUpData): GameModel => Unit =
    _ => ()
}

final case class StartUpData(messageA: String, messageB: String)
final case class GameModel(sceneA: MessageA, sceneB: MessageB)
