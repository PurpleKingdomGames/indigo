package indigogame

import indigo._
import indigo.gameengine.GameEngine
import indigogame.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  */
trait IndigoSandbox[StartupData, Model] extends GameLauncher {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  def setup(assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(context: FrameContext, model: Model): GlobalEvent => Outcome[Model]

  def present(context: FrameContext, model: Model): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, Unit] = {

    val updateViewModel: (FrameContext, Model, Unit) => Outcome[Unit] =
      (_, _, vm) => Outcome(vm)

    val frameProcessor: StandardFrameProcessor[Model, Unit] =
      new StandardFrameProcessor(
        (ctx, m) => (e: GlobalEvent) => update(ctx, m)(e),
        updateViewModel,
        (ctx, m, _) => present(ctx, m)
      )

    new GameEngine[StartupData, StartupErrors, Model, Unit](
      fonts,
      animations,
      (ac: AssetCollection) => (d: Dice) => (_: Map[String, String]) => setup(ac, d),
      (sd: StartupData) => initialModel(sd),
      (_: StartupData) => (_: Model) => (),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit =
    indigoGame.start(config, Future(None), assets, Future(Set()))(flags)

}
