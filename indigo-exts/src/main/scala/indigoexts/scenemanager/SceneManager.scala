package indigoexts.scenemanager

import indigo.time.GameTime
import indigo.gameengine.Outcome
import indigo.gameengine.events.{FrameInputEvents, GlobalEvent}
import indigo.gameengine.scenegraph.SceneUpdateFragment
import indigo.runtime.IndigoLogger

class SceneManager[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  // Scene delegation
  def updateModel(gameTime: GameTime, model: GameModel): GlobalEvent => Outcome[GameModel] = {
    case SceneEvent.Next =>
      finderInstance = finderInstance.forward
      Outcome(model)

    case SceneEvent.Previous =>
      finderInstance = finderInstance.backward
      Outcome(model)

    case SceneEvent.JumpTo(name) =>
      finderInstance = finderInstance.jumpToSceneByName(name)
      Outcome(model)

    case event =>
      scenes.findScene(finderInstance.current.name) match {
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
          Outcome(model)

        case Some(scene) =>
          Scene.updateModel(scene, gameTime, model)(event)
      }
  }

  def updateViewModel(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): Outcome[ViewModel] =
    scenes.findScene(finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        Outcome(viewModel)

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

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel]): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes))

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel], initialScene: SceneName): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
