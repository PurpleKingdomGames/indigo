package indigoexts.entry

import indigo._
import indigoexts.scenemanager.{SceneManager, SceneName, Scene}
import indigo.gameengine.GameEngine
import indigo.gameengine.StandardFrameProcessor

import scala.concurrent.Future

// Using Scala.js, so this is just to make the compiler happy.
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * A trait representing a game with scene management baked in
  * @tparam StartupData The class type representing your successful startup data
  * @tparam Model The class type representing your game's model
  * @tparam ViewModel The class type representing your game's view model
  */
trait IndigoGameWithScenes[StartupData, Model, ViewModel] {

  val scenes: NonEmptyList[Scene[Model, ViewModel]]

  val initialScene: Option[SceneName]

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animation]

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def initialViewModel(startupData: StartupData): Model => ViewModel

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, ViewModel] = {
    val sceneManager: SceneManager[Model, ViewModel] =
      initialScene match {
        case Some(name) =>
          SceneManager(scenes, name)

        case None =>
          SceneManager(scenes, scenes.head.name)
      }

    val frameProcessor: StandardFrameProcessor[Model, ViewModel] =
      StandardFrameProcessor(
        (gameTime: GameTime, model: Model, dice: Dice) => sceneManager.updateModel(gameTime, model, dice),
        (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice) => sceneManager.updateViewModel(gameTime, model, viewModel, frameInputEvents, dice),
        (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents) => sceneManager.updateView(gameTime, model, viewModel, frameInputEvents)
      )

    new GameEngine[StartupData, StartupErrors, Model, ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      (ac: AssetCollection) => setup(ac),
      initialModel,
      initialViewModel,
      frameProcessor
    )
  }

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
