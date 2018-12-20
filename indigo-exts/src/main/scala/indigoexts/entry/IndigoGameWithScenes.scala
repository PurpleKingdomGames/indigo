package indigoexts.entry

import indigo.gameengine.assets.AssetCollection
import indigo.gameengine.events.FrameInputEvents
import indigo.gameengine.scenegraph.Animations
import indigo.gameengine.scenegraph.datatypes.FontInfo
import indigo.gameengine._
import indigo.gameengine.subsystems.SubSystem
import indigo.shared.{AssetType, GameConfig}
import indigoexts.scenemanager.{SceneManager, SceneName, ScenesList}

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

  val scenes: ScenesList[Model, ViewModel, _, _]

  val initialScene: Option[SceneName]

  val config: GameConfig

  val assets: Set[AssetType]

  val fonts: Set[FontInfo]

  val animations: Set[Animations]

  val subSystems: Set[SubSystem]

  def setup(assetCollection: AssetCollection): Startup[StartupErrors, StartupData]

  def initialModel(startupData: StartupData): Model

  def initialViewModel(startupData: StartupData): Model => ViewModel

  private def indigoGame: GameEngine[StartupData, StartupErrors, Model, ViewModel] = {
    val sceneManager: SceneManager[Model, ViewModel] =
      initialScene match {
        case Some(name) =>
          SceneManager(scenes, name)

        case None =>
          SceneManager(scenes)
      }

    new GameEngine[StartupData, StartupErrors, Model, ViewModel](
      config,
      Future(None),
      assets,
      Future(Set()),
      fonts,
      animations,
      subSystems,
      (ac: AssetCollection) => setup(ac),
      initialModel,
      (gameTime: GameTime, model: Model) => sceneManager.updateModel(gameTime, model),
      initialViewModel,
      (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents) => sceneManager.updateViewModel(gameTime, model, viewModel, frameInputEvents),
      (gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents) => sceneManager.updateView(gameTime, model, viewModel, frameInputEvents)
    )
  }

  def main(args: Array[String]): Unit =
    indigoGame.start()

}
