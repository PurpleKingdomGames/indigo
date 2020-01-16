package indigoexts.scenemanager

import indigo.shared.time.GameTime
import indigo.shared.Outcome
import indigo.shared.events.{InputState, GlobalEvent}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.IndigoLogger
import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo._
import indigo.shared.dice.Dice
import scala.collection.mutable
import indigoexts.subsystems.SubSystemsRegister

class SceneManager[GameModel, ViewModel](scenes: NonEmptyList[Scene[GameModel, ViewModel]], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val subSystemStates: mutable.HashMap[SceneName, SubSystemsRegister] = {
    val m = mutable.HashMap[SceneName, SubSystemsRegister]()
    scenes.toList.foreach { s =>
      m.put(s.name, SubSystemsRegister().add(s.sceneSubSystems.toList))
    }
    m
  }

  // Scene delegation
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
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
          val subsystemOutcomeEvents = subSystemStates
            .get(scene.name)
            .map { ssr =>
              val out = ssr.update(gameTime, dice)(event)
              subSystemStates.put(scene.name, out.state)
              out.globalEvents
            }
            .getOrElse(Nil)

          Scene
            .updateModel(scene, gameTime, model, dice)(event)
            .addGlobalEvents(subsystemOutcomeEvents)
      }
  }

  def updateViewModel(gameTime: GameTime, model: GameModel, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        Outcome(viewModel)

      case Some(scene) =>
        Scene.updateViewModel(scene, gameTime, model, viewModel, inputState, dice)
    }

  def updateView(gameTime: GameTime, model: GameModel, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        SceneUpdateFragment.empty

      case Some(scene) =>
        val subsystemView = subSystemStates
          .get(scene.name)
          .map { ssr =>
            ssr.render(gameTime)
          }
          .getOrElse(SceneUpdateFragment.empty)

        Scene.updateView(scene, gameTime, model, viewModel, inputState) |+| subsystemView
    }

}

object SceneManager {

  def apply[GameModel, ViewModel](scenes: NonEmptyList[Scene[GameModel, ViewModel]], initialScene: SceneName): SceneManager[GameModel, ViewModel] =
    new SceneManager[GameModel, ViewModel](scenes, SceneFinder.fromScenes[GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
