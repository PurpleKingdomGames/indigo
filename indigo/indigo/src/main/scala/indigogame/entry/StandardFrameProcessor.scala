package indigogame.entry

import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.gameengine.FrameProcessor
import indigo.shared.BoundaryLocator
import indigo.shared.FrameContext

final class StandardFrameProcessor[Model, ViewModel](
    modelUpdate: (FrameContext, Model) => GlobalEvent => Outcome[Model],
    viewModelUpdate: (FrameContext, Model, ViewModel) => Outcome[ViewModel],
    viewUpdate: (FrameContext, Model, ViewModel) => SceneUpdateFragment
) extends FrameProcessor[Model, ViewModel] {

  def updateModel(frameContext: FrameContext, model: Model): GlobalEvent => Outcome[Model] =
    modelUpdate(frameContext, model)

  def updateViewModel(frameContext: FrameContext, model: Model, viewModel: ViewModel): Outcome[ViewModel] =
    viewModelUpdate(frameContext, model, viewModel)

  def updateView(frameContext: FrameContext, model: Model, viewModel: ViewModel): SceneUpdateFragment =
    viewUpdate(frameContext, model, viewModel)

  def run(
      model: Model,
      viewModel: ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] = {

    val frameContext = new FrameContext(gameTime, dice, inputState, boundaryLocator)

    val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
      acc.flatMapState { next =>
        updateModel(frameContext, next)(e)
      }
    }

    val updatedViewModel: Outcome[ViewModel] =
      updateViewModel(frameContext, updatedModel.state, viewModel)

    val view: SceneUpdateFragment =
      updateView(frameContext, updatedModel.state, updatedViewModel.state)

    Outcome.combine3(updatedModel, updatedViewModel, Outcome(Some(view)))
  }

  def runSkipView(
      model: Model,
      viewModel: ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] = {

    val frameContext = new FrameContext(gameTime, dice, inputState, boundaryLocator)

    val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
      acc.flatMapState { next =>
        updateModel(frameContext, next)(e)
      }
    }

    Outcome.combine3(updatedModel, Outcome(viewModel), Outcome(None))
  }
}
