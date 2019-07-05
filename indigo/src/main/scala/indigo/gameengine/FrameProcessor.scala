package indigo.gameengine

import indigo.shared.dice.Dice
import indigo.shared.Outcome
import indigo.shared.time.GameTime
import indigo.shared.events.{FrameInputEvents, GlobalEvent, Signals}
import indigo.shared.scenegraph.SceneUpdateFragment

trait FrameProcessor[Model, ViewModel] {
  def run: (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment])
  def runSkipView: (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment])
}

trait StandardFrameProcessor[Model, ViewModel] extends FrameProcessor[Model, ViewModel] {

  def updateModel(gameTime: GameTime, model: Model, dice: Dice): GlobalEvent => Outcome[Model]

  def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[ViewModel]

  def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment

  def run: (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment]) =
    StandardFrameProcessor.run(this)

  def runSkipView: (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment]) =
    StandardFrameProcessor.runSkipView(this)
}

object StandardFrameProcessor {

  def apply[Model, ViewModel](
      modelUpdate: (GameTime, Model, Dice) => GlobalEvent => Outcome[Model],
      viewModelUpdate: (GameTime, Model, ViewModel, FrameInputEvents, Dice) => Outcome[ViewModel],
      viewUpdate: (GameTime, Model, ViewModel, FrameInputEvents) => SceneUpdateFragment
  ): StandardFrameProcessor[Model, ViewModel] =
    new StandardFrameProcessor[Model, ViewModel] {

      def updateModel(gameTime: GameTime, model: Model, dice: Dice): GlobalEvent => Outcome[Model] =
        modelUpdate(gameTime, model, dice)

      def updateViewModel(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents, dice: Dice): Outcome[ViewModel] =
        viewModelUpdate(gameTime, model, viewModel, frameInputEvents, dice)

      def updateView(gameTime: GameTime, model: Model, viewModel: ViewModel, frameInputEvents: FrameInputEvents): SceneUpdateFragment =
        viewUpdate(gameTime, model, viewModel, frameInputEvents)

    }

  def run[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment]) =
    (model, viewModel) =>
      (gameTime, globalEvents, signals, dice) => {
        val events: FrameInputEvents =
          FrameInputEvents(globalEvents, signals)

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

        (updatedModel |+| updatedViewModel, Some(view))
      }

  def runSkipView[Model, ViewModel](
      standardFrameProcessor: StandardFrameProcessor[Model, ViewModel]
  ): (Model, ViewModel) => (GameTime, List[GlobalEvent], Signals, Dice) => (Outcome[(Model, ViewModel)], Option[SceneUpdateFragment]) =
    (model, viewModel) =>
      (gameTime, globalEvents, signals, dice) => {
        val events: FrameInputEvents =
          FrameInputEvents(globalEvents, signals)

        val updatedModel: Outcome[Model] = globalEvents.foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMapState { next =>
            standardFrameProcessor.updateModel(gameTime, next, dice)(e)
          }
        }

        val updatedViewModel: Outcome[ViewModel] =
          updatedModel.flatMapState { m =>
            standardFrameProcessor.updateViewModel(gameTime, m, viewModel, events, dice)
          }

        (updatedModel |+| updatedViewModel, None)
      }

}
