package indigoexts.scenemanager

import indigo.gameengine.{GameTime, UpdatedModel, UpdatedViewModel}
import indigo.gameengine.events.{FrameInputEvents, GameEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.runtime.IndigoLogger

class SceneManager[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  // Scene delegation
  def updateModel(gameTime: GameTime, model: GameModel): GameEvent => UpdatedModel[GameModel] = {
    case SceneEvent.Next =>
      finderInstance = finderInstance.forward
      UpdatedModel(model, Nil)

    case SceneEvent.Previous =>
      finderInstance = finderInstance.backward
      UpdatedModel(model, Nil)

    case SceneEvent.JumpTo(name) =>
      finderInstance = finderInstance.jumpToSceneByName(name)
      UpdatedModel(model, Nil)

    case event =>
      scenes.findScene(finderInstance.current.name) match {
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
          UpdatedModel(model, Nil)

        case Some(scene) =>
          Scene.updateModel(scene, gameTime, model)(event)
      }
  }

  def updateViewModel(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): UpdatedViewModel[ViewModel] =
    scenes.findScene(finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        UpdatedViewModel(viewModel, Nil)

      case Some(scene) =>
        Scene.updateViewModel(scene, gameTime, model, viewModel, frameInputEvents)
    }

  def updateView(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scenes.findScene(finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        SceneUpdateFragment.empty

      case Some(scene) =>
        Scene.updateView(scene, gameTime, model, viewModel, frameInputEvents)
    }

}

object SceneManager {

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _]): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes))

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _], initialScene: SceneName): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
