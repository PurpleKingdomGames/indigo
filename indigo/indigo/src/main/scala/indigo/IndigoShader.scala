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

  /** Your shader's configuration settings.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  val assets: Set[AssetType]

  /** A fixed set of custom shaders you will be able to render with
    */
  val shader: Shader

  // TODO: start in fullscreen flag
  // TODO: Fullscreen key mapping flag
  // TODO: Accept asset path, shader details?
  // TODO: Optionally show FPS?
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

  // Store the viewpoint size
  private def initialModel(startupData: IndigoShaderBootData): Outcome[IndigoShaderModel] =
    Outcome(IndigoShaderModel(startupData.viewport))

  private def updateModel(
      context: FrameContext[IndigoShaderBootData],
      model: IndigoShaderModel
  ): GlobalEvent => Outcome[IndigoShaderModel] =
    // TODO: Intercept viewpoint resizes and update
    _ => Outcome(model)

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
