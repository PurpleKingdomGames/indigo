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
trait IndigoShader extends GameLauncher[Unit, Unit, Unit] {

  /** Your shader's configuration settings.
    */
  val config: GameConfig

  /** A fixed set of assets that will be loaded before the game starts
    */
  val assets: Set[AssetType]

  /** A fixed set of custom shaders you will be able to render with
    */
  val shader: Shader

  // TODO: Accept size flags
  private def boot(flags: Map[String, String]): Outcome[BootResult[Unit]] =
    Outcome(BootResult(config, ()))

  private def setup(bootData: Unit, assetCollection: AssetCollection, dice: Dice): Outcome[Startup[Unit]] =
    Outcome(Startup.Success(()))

  // Store the viewpoint size
  private def initialModel(startupData: Unit): Outcome[Unit] =
    Outcome(())

  private def updateModel(context: FrameContext[Unit], model: Unit): GlobalEvent => Outcome[Unit] =
    // TODO: Intercept viewpoint resizes and update
    _ => Outcome(model)

  // TODO: Draw a BlankEntity that fills the screen and renders the shader
  private def present(context: FrameContext[Unit], model: Unit): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
    )

  private def indigoGame(boot: BootResult[Unit]): GameEngine[Unit, Unit, Unit] = {

    val updateViewModel: (FrameContext[Unit], Unit, Unit) => GlobalEvent => Outcome[Unit] =
      (_, _, vm) => _ => Outcome(vm)

    val eventFilters: EventFilters =
      EventFilters(
        { case e => Some(e) },
        { case _ => None }
      )

    val frameProcessor: StandardFrameProcessor[Unit, Unit, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(),
        eventFilters,
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[Unit, Unit, Unit](
      Set(),
      Set(),
      Set(shader),
      (ac: AssetCollection) => (d: Dice) => setup(boot.bootData, ac, d),
      (sd: Unit) => initialModel(sd),
      (_: Unit) => (_: Unit) => Outcome(()),
      frameProcessor,
      Batch.empty
    )
  }

  protected def ready(flags: Map[String, String]): Element => GameEngine[Unit, Unit, Unit] =
    parentElement =>
      boot(flags) match
        case oe @ Outcome.Error(e, _) =>
          IndigoLogger.error("Error during boot - Halting")
          IndigoLogger.error(oe.reportCrash)
          throw e

        case Outcome.Result(b, evts) =>
          indigoGame(b).start(parentElement, b.gameConfig, Future(None), b.assets, Future(Set()), evts)

}
