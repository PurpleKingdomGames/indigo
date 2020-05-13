package indigogame.entry

import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.time.GameTime
import indigo.gameengine.FrameProcessor
import indigo.shared.BoundaryLocator

final class StandardFrameProcessor[Model, ViewModel](
    modelUpdate: (GameTime, Model, InputState, Dice) => GlobalEvent => Outcome[Model],
    viewModelUpdate: (GameTime, Model, ViewModel, InputState, Dice) => Outcome[ViewModel],
    viewUpdate: (GameTime, Model, ViewModel, InputState, BoundaryLocator) => SceneUpdateFragment
) extends FrameProcessor[Model, ViewModel] {

  def updateModel(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model] =
    modelUpdate(gameTime, model, inputState, dice)

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
    viewModelUpdate(gameTime, model, viewModel, inputState, dice)

  def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, boundaryLocator: BoundaryLocator): SceneUpdateFragment =
    viewUpdate(gameTime, model, viewModel, inputState, boundaryLocator)

  def run(
      model: Model,
      viewModel: ViewModel,
      gameTime: GameTime,
      globalEvents: List[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator
  ): Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] = {

    val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
      acc.flatMapState { next =>
        updateModel(gameTime, next, inputState, dice)(e)
      }
    }

    val updatedViewModel: Outcome[ViewModel] =
      updateViewModel(gameTime, updatedModel.state, viewModel, inputState, dice)

    val view: SceneUpdateFragment =
      updateView(gameTime, updatedModel.state, updatedViewModel.state, inputState, boundaryLocator)

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

    val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
      acc.flatMapState { next =>
        updateModel(gameTime, next, inputState, dice)(e)
      }
    }

    Outcome.combine3(updatedModel, Outcome(viewModel), Outcome(None))
  }
}
