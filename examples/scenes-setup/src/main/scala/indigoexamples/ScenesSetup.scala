package indigoexamples

import indigo._
import indigo.scenes._
import indigoextras.subsystems.FPSCounter

import scala.scalajs.js.annotation._

@JSExportTopLevel("IndigoGame")
object ScenesSetup extends IndigoGame[Unit, StartUpData, GameModel, Unit]:

  val targetFPS: FPS = FPS.`30`

  def scenes(bootData: Unit): NonEmptyList[Scene[StartUpData, GameModel, Unit]] =
    NonEmptyList(SceneA, SceneB)

  def initialScene(bootData: Unit): Option[SceneName] = Option(SceneA.name)

  def eventFilters: EventFilters =
    EventFilters.Permissive

  def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(
      BootResult
        .noData(
          defaultGameConfig
            .withClearColor(RGBA.fromHexString("0xAA3399"))
            .withFrameRateLimit(targetFPS)
        )
        .withAssets(AssetType.Image(FontStuff.fontName, AssetPath("assets/boxy_font.png")))
        .withFonts(FontStuff.fontInfo)
        .withSubSystems(FPSCounter(Point(10, 360), targetFPS))
    )

  def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[StartUpData]] =
    Outcome(Startup.Success(StartUpData("Scene A!", "Scene B?")))

  def initialModel(startupData: StartUpData): Outcome[GameModel] =
    Outcome(
      GameModel(
        sceneA = MessageA(startupData.messageA),
        sceneB = MessageB(startupData.messageB)
      )
    )

  def initialViewModel(startupData: StartUpData, model: GameModel): Outcome[Unit] =
    Outcome(())

  def updateModel(context: FrameContext[StartUpData], model: GameModel): GlobalEvent => Outcome[GameModel] =
    _ => Outcome(model)

  def updateViewModel(context: FrameContext[StartUpData], model: GameModel, viewModel: Unit): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(context: FrameContext[StartUpData], model: GameModel, viewModel: Unit): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment.empty)

final case class StartUpData(messageA: String, messageB: String)
final case class GameModel(sceneA: MessageA, sceneB: MessageB)
