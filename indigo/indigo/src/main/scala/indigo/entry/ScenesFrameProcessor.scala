package indigo.entry

import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.gameengine.FrameProcessor
import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.scenes.SceneManager

// @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class ScenesFrameProcessor[StartUpData, Model, ViewModel](
    subSystemsRegister: SubSystemsRegister,
    sceneManager: SceneManager[StartUpData, Model, ViewModel]
) extends FrameProcessor[StartUpData, Model, ViewModel] {

  def run(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)] = {

    val frameContext = new FrameContext[StartUpData](gameTime, dice, inputState, boundaryLocator, startUpData)

    val updatedModel: Outcome[Model] =
      globalEvents
        .map(sceneManager.eventFilters.modelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMapState { next =>
            sceneManager.updateModel(frameContext, next)(e)
          }
        }

    val subSystemEvents: List[GlobalEvent] =
      subSystemsRegister.update(frameContext.forSubSystems, globalEvents).globalEvents ++
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)

    val updatedViewModel: Outcome[ViewModel] =
      globalEvents
        .map(sceneManager.eventFilters.viewModelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(viewModel)) { (acc, e) =>
          acc.flatMapState { next =>
            sceneManager.updateViewModel(frameContext, updatedModel.state, next)(e)
          }
        }

    val view: Outcome[SceneUpdateFragment] =
      Outcome.merge(
        sceneManager.updateView(frameContext, updatedModel.state, updatedViewModel.state),
        subSystemsRegister.present(frameContext.forSubSystems)
      )(_ |+| _)

    Outcome
      .combine3(updatedModel, updatedViewModel, view)
      .addGlobalEvents(subSystemEvents)
  }

  def runSkipView(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel)] = {

    val frameContext = new FrameContext[StartUpData](gameTime, dice, inputState, boundaryLocator, startUpData)

    val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
      acc.flatMapState { next =>
        sceneManager.updateModel(frameContext, next)(e)
      }
    }

    val subSystemEvents: List[GlobalEvent] =
      subSystemsRegister.update(frameContext.forSubSystems, globalEvents).globalEvents ++
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)

    Outcome
      .combine(updatedModel, Outcome(viewModel))
      .addGlobalEvents(subSystemEvents)
  }
}
