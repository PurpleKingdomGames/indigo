package indigo

import indigo._
import indigo.entry.StandardFrameProcessor
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystemsRegister
import org.scalajs.dom.Element
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._

import scala.concurrent.Future

/** A trait representing a shader that fills the available window.
  */
trait IndigoShader extends GameLauncher[IndigoShaderBootData, IndigoShaderModel, Unit] {

  /** Your shader's default configuration settings, values like the viewport size can be overriden with flags.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  val assets: Set[AssetType]

  /** The shader you want to render
    */
  val shader: Shader

  // TODO: start in fullscreen flag
  // TODO: Fullscreen key mapping flag
  // TODO: Accept asset path, shader details?
  // TODO: Optionally show FPS?
  // TODO: Do not load all standard shaders - just normal blend?
  private def boot(flags: Map[String, String]): Outcome[BootResult[IndigoShaderBootData]] =
    val width  = flags.get("width").map(_.toInt).getOrElse(config.viewport.width)
    val height = flags.get("height").map(_.toInt).getOrElse(config.viewport.height)

    val configWithOverrides =
      config.withViewport(width, height)

    val bootData =
      IndigoShaderBootData(Size(width, height))

    Outcome(
      BootResult(
        configWithOverrides,
        bootData
      )
        .withShaders(shader)
        .withAssets(assets)
    )

  private def setup(
      bootData: IndigoShaderBootData,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[IndigoShaderBootData]] =
    Outcome(
      Startup.Success(
        bootData
      )
    )

  private def initialModel(startupData: IndigoShaderBootData): Outcome[IndigoShaderModel] =
    Outcome(IndigoShaderModel(startupData.viewport))

  private def updateModel(
      context: FrameContext[IndigoShaderBootData],
      model: IndigoShaderModel
  ): GlobalEvent => Outcome[IndigoShaderModel] = {
    case ViewportResize(vp) =>
      Outcome(model.copy(viewport = vp.size))

    case _ =>
      Outcome(model)
  }

  private def present(
      context: FrameContext[IndigoShaderBootData],
      model: IndigoShaderModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        BlankEntity(model.viewport, ShaderData(shader.id))
      )
    )

  private def indigoGame(
      boot: BootResult[IndigoShaderBootData]
  ): GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit] = {

    val updateViewModel: (FrameContext[IndigoShaderBootData], IndigoShaderModel, Unit) => GlobalEvent => Outcome[Unit] =
      (_, _, vm) => _ => Outcome(vm)


    // TODO: Only accept the events we care about?
    val eventFilters: EventFilters =
      EventFilters.Permissive

    val frameProcessor: StandardFrameProcessor[IndigoShaderBootData, IndigoShaderModel, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(),
        eventFilters,
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit](
      Set(),
      Set(),
      Set(shader),
      (ac: AssetCollection) => (d: Dice) => setup(boot.bootData, ac, d),
      (sd: IndigoShaderBootData) => initialModel(sd),
      (_: IndigoShaderBootData) => (_: IndigoShaderModel) => Outcome(()),
      frameProcessor,
      Batch.empty
    )
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
  protected def ready(
      flags: Map[String, String]
  ): Element => GameEngine[IndigoShaderBootData, IndigoShaderModel, Unit] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}

final case class IndigoShaderBootData(viewport: Size)
final case class IndigoShaderModel(viewport: Size)
