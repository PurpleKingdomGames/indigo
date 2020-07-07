package indigo.scenes

import indigo.shared.Outcome
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.IndigoLogger
import indigo.shared.collections.NonEmptyList
import indigo.shared.EqualTo._
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.FrameContext
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.shared.events.EventFilters

class SceneManager[StartUpData, GameModel, ViewModel](scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]], scenesFinder: SceneFinder) {

  // Scene management
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var finderInstance: SceneFinder = scenesFinder

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
  private val subSystemStates: Map[SceneName, SubSystemsRegister] =
    scenes.toList.map { s =>
      val r = new SubSystemsRegister(Nil)
      r.register(s.subSystems.toList)
      (s.name -> r)
    }.toMap

  // Scene delegation
  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def updateModel(frameContext: FrameContext[StartUpData], model: GameModel): GlobalEvent => Outcome[GameModel] = {
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
              ssr.update(frameContext.forSubSystems)(event).globalEvents
            }
            .getOrElse(Nil)

          Scene
            .updateModel(scene, frameContext, model)(event)
            .addGlobalEvents(subsystemOutcomeEvents)
      }
  }

  def updateViewModel(frameContext: FrameContext[StartUpData], model: GameModel, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        _ => Outcome(viewModel)

      case Some(scene) =>
        Scene.updateViewModel(scene, frameContext, model, viewModel)
    }

  def updateView(frameContext: FrameContext[StartUpData], model: GameModel, viewModel: ViewModel): SceneUpdateFragment =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name.name)
        SceneUpdateFragment.empty

      case Some(scene) =>
        val subsystemView = subSystemStates
          .get(scene.name)
          .map { ssr =>
            ssr.present(frameContext.forSubSystems)
          }
          .getOrElse(SceneUpdateFragment.empty)

        Scene.updateView(scene, frameContext, model, viewModel) |+| subsystemView
    }

  val defaultFilter: GlobalEvent => Option[GlobalEvent] =
    (e: GlobalEvent) => Some(e)

  def eventFilters: EventFilters =
    scenes.find(_.name === finderInstance.current.name) match {
      case None =>
        EventFilters.Default

      case Some(value) =>
        value.eventFilters
    }

}

object SceneManager {

  def apply[StartUpData, GameModel, ViewModel](scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]], initialScene: SceneName): SceneManager[StartUpData, GameModel, ViewModel] =
    new SceneManager[StartUpData, GameModel, ViewModel](scenes, SceneFinder.fromScenes[StartUpData, GameModel, ViewModel](scenes).jumpToSceneByName(initialScene))

}
