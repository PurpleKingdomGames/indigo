package com.purplekingdomgames.indigoexts.scenemanager

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.{FrameInputEvents, GameEvent}
import com.purplekingdomgames.indigo.gameengine.scenegraph.SceneUpdateFragment
import com.purplekingdomgames.indigo.runtime.IndigoLogger

class SceneManager[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  // Scene delegation
  def updateModel(gameTime: GameTime, model: GameModel): GameEvent => GameModel = {
    case NextScene =>
      finderInstance = finderInstance.forward
      model

    case PreviousScene =>
      finderInstance = finderInstance.backward
      model

    case JumpToScene(name) =>
      finderInstance = finderInstance.jumpToSceneByName(name)
      model

    case event =>
      scenes.findScene(finderInstance.current.name) match {
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
          model

        case Some(scene) =>
          scene.updateModelDelegate(gameTime, model)(event)
      }
  }

  def updateViewModel(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): ViewModel =
    scenes.findScene(finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        viewModel

      case Some(scene) =>
        scene.updateViewModelDelegate(gameTime, model, viewModel, frameInputEvents)
    }

  def updateView(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scenes.findScene(finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        SceneUpdateFragment.empty

      case Some(scene) =>
        scene.updateViewDelegate(gameTime, model, viewModel, frameInputEvents)
    }

}

object SceneManager {

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _]): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes))

  def apply[GameModel, ViewModel](scenes: ScenesList[GameModel, ViewModel, _, _], initialScene: SceneName): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
