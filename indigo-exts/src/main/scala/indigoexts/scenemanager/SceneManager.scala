package indigoexts.scenemanager

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{FrameInputEvents, GlobalEvent}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.IndigoLogger
import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo._
import indigo.shared.dice.Dice

class SceneManager[GameModel, ViewModel](scenes: NonEmptyList[Scene[GameModel, ViewModel]], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  // Scene delegation
  def updateModel(gameTime: GameTime, model: GameModel, dice: Dice): GlobalEvent => Outcome[GameModel] = {
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
      scenes.find(_.name === finderInstance.current.name) match {
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
          Outcome(model)

        case Some(scene) =>
          Scene.updateModel(scene, gameTime, model, dice)(event)
      }
  }

  def updateViewModel(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[ViewModel] =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        Outcome(viewModel)

      case Some(scene) =>
        Scene.updateViewModel(scene, gameTime, model, viewModel, frameInputEvents, dice)
    }

  def updateView(gameTime: GameTime, model: GameModel, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        SceneUpdateFragment.empty

      case Some(scene) =>
        Scene.updateView(scene, gameTime, model, viewModel, frameInputEvents)
    }

}

object SceneManager {

  def apply[GameModel, ViewModel](scenes: NonEmptyList[Scene[GameModel, ViewModel]], initialScene: SceneName): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
