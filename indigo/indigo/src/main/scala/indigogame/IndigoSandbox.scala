package indigogame

import indigo._
import indigo.gameengine.GameEngine
import indigogame.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import indigo.shared.BoundaryLocator

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

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model]

  def present(gameTime: GameTime, model: Model, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, Unit] = {

    val updateViewModel: (GameTime, Model, Unit, InputState, Dice) => Outcome[Unit] =
      (_, _, vm, _, _) => Outcome(vm)

    val frameProcessor: StandardFrameProcessor[Model, Unit] =
      new StandardFrameProcessor(
        update,
        updateViewModel,
        (gt, m, _, is, bl) => present(gt, m, is, bl)
      )

    new GameEngine[StartupData, StartupErrors, Model, Unit](
      fonts,
      animations,
      (ac: AssetCollection) => (_: Map[String, String]) => setup(ac),
      (sd: StartupData) => initialModel(sd),
      (_: StartupData) => (_: Model) => (),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit =
    indigoGame.start(config, Future(None), assets, Future(Set()))(flags)

}
