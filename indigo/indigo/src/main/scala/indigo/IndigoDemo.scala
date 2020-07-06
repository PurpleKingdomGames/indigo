package indigo

import indigo._
import indigo.gameengine.GameEngine
import indigo.shared.subsystems.SubSystemsRegister
import indigo.entry.GameWithSubSystems
import indigo.entry.StandardFrameProcessor

// Indigo is Scala.js only at the moment, revisit if/when we go to the JVM
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartUpData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoDemo[BootData, StartUpData, Model, ViewModel] extends GameLauncher {

  def boot(flags: Map[String, String]): BootResult[BootData]

  def setup(bootData: BootData, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartUpData]

  def initialModel(startupData: StartUpData): Model

  def initialViewModel(startupData: StartUpData, model: Model): ViewModel

  def updateModel(context: FrameContext[StartUpData], model: Model): GlobalEvent => Outcome[Model]

  def updateViewModel(context: FrameContext[StartUpData], model: Model, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel]

  def present(context: FrameContext[StartUpData], model: Model, viewModel: ViewModel): SceneUpdateFragment

  private val subSystemsRegister: SubSystemsRegister =
    new SubSystemsRegister(Nil)

  private def indigoGame(bootUp: BootResult[BootData]): GameEngine[StartUpData, StartupErrors, Model, ViewModel] = {
    subSystemsRegister.register(bootUp.subSystems.toList)

    val frameProcessor: StandardFrameProcessor[StartUpData, Model, ViewModel] =
      new StandardFrameProcessor(
        GameWithSubSystems.update(subSystemsRegister, updateModel),
        GameWithSubSystems.updateViewModel(updateViewModel),
        GameWithSubSystems.present(subSystemsRegister, present)
      )

    new GameEngine[StartUpData, StartupErrors, Model, ViewModel](
      bootUp.fonts,
      bootUp.animations,
      (ac: AssetCollection) => (d: Dice) => setup(bootUp.bootData, ac, d),
      (sd: StartUpData) => initialModel(sd),
      (sd: StartUpData) => (m: Model) => initialViewModel(sd, m),
      frameProcessor
    )
  }

  final protected def ready(flags: Map[String, String]): Unit = {
    val b = boot(flags)
    indigoGame(b).start(b.gameConfig, Future(None), b.assets, Future(Set()))
  }

}
