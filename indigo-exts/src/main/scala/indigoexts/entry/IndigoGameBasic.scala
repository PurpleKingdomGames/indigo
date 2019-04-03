package indigoexts.entry

import indigo.gameengine.assets.AssetCollection
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.datatypes.FontInfo
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.gameengine.scenegraph.animation.Animation
import indigo.gameengine._
import indigo.gameengine.subsystems.SubSystem
import indigo.shared.{AssetType, GameConfig}
import indigo.time.GameTime

import scala.concurrent.Future

// Using Scala.js, so this is just to make the compiler happy.
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A trait representing a minimal set of functions to get your game running
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoGameBasic[StartupData, Model, ViewModel] {

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  val subSystems: Set[SubSystem]

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(gameTime: GameTime, model: Model): GlobalEvent => Outcome[Model]

  def initialViewModel(startupData: StartupData): Model => ViewModel

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): Outcome[ViewModel]

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, ViewModel] =
    GameEngine[StartupData, StartupErrors, Model, ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      subSystems,
      (ac: AssetCollection) => setup(ac),
      initialModel,
      update,
      initialViewModel,
      updateViewModel,
      (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents) => present(gameTime, model, viewModel, frameInputEvents)
    )

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
