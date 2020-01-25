package indigo.gameengine

import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{GlobalEvent, InputState}
import indigo.shared.scenegraph.SceneUpdateFragment

trait FrameProcessor[Model, ViewModel] {
  def run: (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]
  def runSkipView: (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]
}

trait StandardFrameProcessor[Model, ViewModel] extends FrameProcessor[Model, ViewModel] {

  def updateModel(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model]

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel]

  def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment

  def run: (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    StandardFrameProcessor.run(this)

  def runSkipView: (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    StandardFrameProcessor.runSkipView(this)
}

object StandardFrameProcessor {

  def apply[Model, ViewModel](
      modelUpdate: (GameTime, Model, InputState, Dice) => GlobalEvent => Outcome[Model],
      viewModelUpdate: (GameTime, Model, ViewModel, InputState, Dice) => Outcome[ViewModel],
      viewUpdate: (GameTime, Model, ViewModel, InputState) => SceneUpdateFragment
  ): StandardFrameProcessor[Model, ViewModel] =
    new StandardFrameProcessor[Model, ViewModel] {

      def updateModel(gameTime: GameTime, model: Model, inputState: InputState, dice: Dice): GlobalEvent => Outcome[Model] =
        modelUpdate(gameTime, model, inputState, dice)

      def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState, dice: Dice): Outcome[ViewModel] =
        viewModelUpdate(gameTime, model, viewModel, inputState, dice)

      def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, inputState: InputState): SceneUpdateFragment =
        viewUpdate(gameTime, model, viewModel, inputState)

    }

  def run[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    (model, viewModel, gameTime, globalEvents, inputState, dice) => {

      val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMapState { next =>
          standardFrameProcessor.updateModel(gameTime, next, inputState, dice)(e)
        }
      }

      val updatedViewModel: Outcome[ViewModel] =
        updatedModel.flatMapState { m =>
          standardFrameProcessor.updateViewModel(gameTime, m, viewModel, inputState, dice)
        }

      val view: SceneUpdateFragment =
        standardFrameProcessor.updateView(gameTime, updatedModel.state, updatedViewModel.state, inputState)

      Outcome.combine3(updatedModel, updatedViewModel, Outcome(Some(view)))
    }

  def runSkipView[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel, GameTime, List[GlobalEvent], InputState, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    (model, viewModel, gameTime, globalEvents, inputState, dice) => {

      val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMapState { next =>
          standardFrameProcessor.updateModel(gameTime, next, inputState, dice)(e)
        }
      }

      val updatedViewModel: Outcome[ViewModel] =
        updatedModel.flatMapState { m =>
          standardFrameProcessor.updateViewModel(gameTime, m, viewModel, inputState, dice)
        }

      Outcome.combine3(updatedModel, updatedViewModel, Outcome(None))
    }

}
