package indigo

import indigo._
import indigo.gameengine.GameEngine
import indigo.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import indigo.shared.subsystems.SubSystemsRegister

/**
  * A trait representing a minimal set of functions to get your game running
  */
trait IndigoSandbox[StartUpData, Model] extends GameLauncher {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[StartUpData]

  def initialModel(startupData: StartUpData): Model

  def updateModel(context: FrameContext[StartUpData], model: Model): GlobalEvent => Outcome[Model]

  def present(context: FrameContext[StartUpData], model: Model): Outcome[SceneUpdateFragment]

  private def indigoGame: GameEngine[StartUpData, Model, Unit] = {

    val updateViewModel: (FrameContext[StartUpData], Model, Unit) => GlobalEvent => Outcome[Unit] =
      (_, _, vm) => _ => Outcome(vm)

    val frameProcessor: StandardFrameProcessor[StartUpData, Model, Unit] =
      new StandardFrameProcessor(
        new SubSystemsRegister(Nil),
        EventFilters.Default,
        (ctx, m) => (e: GlobalEvent) => updateModel(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[StartUpData, Model, Unit](
      fonts,
      animations,
      (ac: AssetCollection) => (d: Dice) => setup(ac, d),
      (sd: StartUpData) => initialModel(sd),
      (_: StartUpData) => (_: Model) => (),
      frameProcessor
    )
  }

  // @SuppressWarnings(Array("org.wartremover.warts.GlobalExecutionContext"))
  final protected def ready(flags: Map[String, String]): Unit =
    indigoGame.start(config, Future(None), assets, Future(Set()))

}
