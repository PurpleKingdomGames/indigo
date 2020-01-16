package indigo.gameengine

import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{GlobalEvent, InputSignals}
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.InputEvent

trait FrameProcessor[Model, ViewModel] {
  def run: (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]
  def runSkipView: (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])]
}

trait StandardFrameProcessor[Model, ViewModel] extends FrameProcessor[Model, ViewModel] {

  def updateModel(gameTime: GameTime, model: Model, dice: Dice): GlobalEvent => Outcome[Model]

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputSignals: InputSignals, dice: Dice): Outcome[ViewModel]

  def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, inputSignals: InputSignals): SceneUpdateFragment

  def run: (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    StandardFrameProcessor.run(this)

  def runSkipView: (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    StandardFrameProcessor.runSkipView(this)
}

object StandardFrameProcessor {

  def apply[Model, ViewModel](
      modelUpdate: (GameTime, Model, Dice) => GlobalEvent => Outcome[Model],
      viewModelUpdate: (GameTime, Model, ViewModel, InputSignals, Dice) => Outcome[ViewModel],
      viewUpdate: (GameTime, Model, ViewModel, InputSignals) => SceneUpdateFragment
  ): StandardFrameProcessor[Model, ViewModel] =
    new StandardFrameProcessor[Model, ViewModel] {

      def updateModel(gameTime: GameTime, model: Model, dice: Dice): GlobalEvent => Outcome[Model] =
        modelUpdate(gameTime, model, dice)

      def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, inputSignals: InputSignals, dice: Dice): Outcome[ViewModel] =
        viewModelUpdate(gameTime, model, viewModel, inputSignals, dice)

      def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, inputSignals: InputSignals): SceneUpdateFragment =
        viewUpdate(gameTime, model, viewModel, inputSignals)

    }

  def run[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    (model, viewModel, gameTime, globalEvents, signals, dice) => {
      val events: InputSignals =
        signals.calculateNext(globalEvents.collect { case e: InputEvent => e })

      val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMapState { next =>
          standardFrameProcessor.updateModel(gameTime, next, dice)(e)
        }
      }

      val updatedViewModel: Outcome[ViewModel] =
        updatedModel.flatMapState { m =>
          standardFrameProcessor.updateViewModel(gameTime, m, viewModel, events, dice)
        }

      val view: SceneUpdateFragment =
        standardFrameProcessor.updateView(gameTime, updatedModel.state, updatedViewModel.state, events)

      Outcome.combine3(updatedModel, updatedViewModel, Outcome(Some(view)))
    }

  def runSkipView[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel, GameTime, List[GlobalEvent], InputSignals, Dice) => Outcome[(Model, ViewModel, Option[SceneUpdateFragment])] =
    (model, viewModel, gameTime, globalEvents, signals, dice) => {
      val events: InputSignals =
        signals.calculateNext(globalEvents.collect { case e: InputEvent => e })

      val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMapState { next =>
          standardFrameProcessor.updateModel(gameTime, next, dice)(e)
        }
      }

      val updatedViewModel: Outcome[ViewModel] =
        updatedModel.flatMapState { m =>
          standardFrameProcessor.updateViewModel(gameTime, m, viewModel, events, dice)
        }

      Outcome.combine3(updatedModel, updatedViewModel, Outcome(None))
    }

}
