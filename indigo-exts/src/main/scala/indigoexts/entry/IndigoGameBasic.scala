package indigoexts.entry

import indigo.gameengine.assets.AssetCollection
import indigo.gameengine.events.{FrameInputEvents, GameEvent}
import indigo.gameengine.scenegraph.datatypes.FontInfo
import indigo.gameengine.scenegraph.{Animations, SceneUpdateFragment}
import indigo.gameengine.{GameEngine, GameTime, Startup, StartupErrors}
import indigo.shared.{AssetType, GameConfig}

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

  val animations: Set[Animations]

  def setup(assetCollection: AssetCollection): Either[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def update(gameTime: GameTime, model: Model): GameEvent => Model

  def initialViewModel(startupData: StartupData): Model => ViewModel

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): ViewModel

  def present(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, ViewModel] =
    new GameEngine[StartupData, StartupErrors, Model, ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      (ac: AssetCollection) => Startup.fromEither(setup(ac)),
      initialModel,
      update,
      initialViewModel,
      updateViewModel,
      (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents) => present(gameTime, model, viewModel, frameInputEvents)
    )

  def registerAnimations(animations: Animations): Unit =
    indigoGame.registerAnimations(animations)

  def registerFont(fontInfo: FontInfo): Unit =
    indigoGame.registerFont(fontInfo)

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
