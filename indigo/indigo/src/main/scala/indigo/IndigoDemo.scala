package indigo

import indigo._
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystem
import indigo.shared.subsystems.SubSystemsRegister
import indigo.entry.GameWithSubSystems
import indigo.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoDemo[FlagData, StartupData, Model, ViewModel] extends GameLauncher {

  def parseFlags(flags: Map[String, String]): FlagData

  def config(flagData: FlagData): GameConfig

  def assets(flagData: FlagData): Set[AssetType]

  def fonts: Set[FontInfo]

  def animations: Set[Animation]

  def subSystems: Set[SubSystem]

  def setup(flagData: FlagData, gameConfig: GameConfig, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def initialViewModel(startupData: StartupData, model: Model): ViewModel

  def updateModel(context: FrameContext, model: Model): GlobalEvent => Outcome[Model]

  def updateViewModel(context: FrameContext, model: Model, viewModel: ViewModel): Outcome[ViewModel]

  def present(context: FrameContext, model: Model, viewModel: ViewModel): SceneUpdateFragment

  private def indigoGame(flagData: FlagData, gameConfig: GameConfig): GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel] = {

    val frameProcessor: StandardFrameProcessor[GameWithSubSystems[Model], ViewModel] =
      new StandardFrameProcessor(
        GameWithSubSystems.update(updateModel),
        GameWithSubSystems.updateViewModel(updateViewModel),
        GameWithSubSystems.present(present)
      )

    new GameEngine[StartupData, StartupErrors, GameWithSubSystems[Model], ViewModel](
      fonts,
      animations,
      (ac: AssetCollection) => (d: Dice) => setup(flagData, gameConfig, ac, d),
      (sd: StartupData) => new GameWithSubSystems(initialModel(sd), new SubSystemsRegister(subSystems.toList)),
      (sd: StartupData) => (m: GameWithSubSystems[Model]) => initialViewModel(sd, m.model),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit = {
    val flagData: FlagData = parseFlags(flags)
    val gameConfig: GameConfig = config(flagData)
    indigoGame(flagData, gameConfig).start(gameConfig, Future(None), assets(flagData), Future(Set()))
  }

}
