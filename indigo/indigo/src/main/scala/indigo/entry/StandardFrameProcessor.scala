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

final class StandardFrameProcessor[StartUpData, Model, ViewModel](
    modelUpdate: (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model],
    viewModelUpdate: (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    viewUpdate: (FrameContext[StartUpData], Model, ViewModel) => SceneUpdateFragment
) extends FrameProcessor[StartUpData, Model, ViewModel] {

  def updateModel(frameContext: FrameContext[StartUpData], model: Model): GlobalEvent => Outcome[Model] =
    modelUpdate(frameContext, model)

  def updateViewModel(frameContext: FrameContext[StartUpData], model: Model, viewModel: ViewModel): GlobalEvent => Outcome[ViewModel] =
    viewModelUpdate(frameContext, model, viewModel)

  def updateView(frameContext: FrameContext[StartUpData], model: Model, viewModel: ViewModel): SceneUpdateFragment =
    viewUpdate(frameContext, model, viewModel)

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
      globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMapState { next =>
          updateModel(frameContext, next)(e)
        }
      }

    val updatedViewModel: Outcome[ViewModel] =
      globalEvents.foldLeft(Outcome(viewModel)) { (acc, e) =>
        acc.flatMapState { next =>
          updateViewModel(frameContext, updatedModel.state, next)(e)
        }
      }

    val view: SceneUpdateFragment =
      updateView(frameContext, updatedModel.state, updatedViewModel.state)

    Outcome.combine3(updatedModel, updatedViewModel, Outcome(view))
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
        updateModel(frameContext, next)(e)
      }
    }

    Outcome.combine(updatedModel, Outcome(viewModel))
  }
}
