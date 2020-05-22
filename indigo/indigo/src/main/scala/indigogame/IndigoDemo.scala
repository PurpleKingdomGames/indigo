package indigogame

import indigo._
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemsRegister
import indigogame.entry.GameWithSubSystems
import indigogame.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoDemo[StartupData, Model, ViewModel] extends GameLauncher {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  val subSystems: Set[SubSystem]

  def setup(assetCollection: AssetCollection, dice: Dice, flags: Map[String, String]): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def initialViewModel(startupData: StartupData, model: Model): ViewModel

  def updateModel(context: FrameContext, model: Model): GlobalEvent => Outcome[Model]

  def updateViewModel(context: FrameContext, model: Model, viewModel: ViewModel): Outcome[ViewModel]

  def present(context: FrameContext, model: Model, viewModel: ViewModel): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameWithSubSystems[Model], ViewModel] =
      new StandardFrameProcessor(
        GameWithSubSystems.update(updateModel),
        GameWithSubSystems.updateViewModel(updateViewModel),
        GameWithSubSystems.present(present)
      )

    new GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel](
      fonts,
      animations,
      (ac: AssetCollection) => (d: Dice) => (flags: Map[String, String]) => setup(ac, d, flags),
      (sd: StartupData) => new GameWithSubSystems(initialModel(sd), new SubSystemsRegister(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd, m.model),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit =
    indigoGame.start(config, Future(None), assets, Future(Set()))(flags)

}
