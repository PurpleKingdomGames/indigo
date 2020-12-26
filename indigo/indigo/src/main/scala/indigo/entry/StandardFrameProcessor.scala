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
import indigo.shared.events.EventFilters
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.subsystems.SubSystemFrameContext._

// @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final case class StandardFrameProcessor[StartUpData, Model, ViewModel](
    subSystemsRegister: SubSystemsRegister,
    eventFilters: EventFilters,
    modelUpdate: (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model],
    viewModelUpdate: (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    viewUpdate: (FrameContext[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model, ViewModel]
    with StandardFrameProcessorFunctions[StartUpData, Model, ViewModel] {

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

    Outcome.join(
      for {
        m  <- processModel(frameContext, model, globalEvents)
        vm <- processViewModel(frameContext, m, viewModel, globalEvents)
        e  <- subSystemsRegister.update(frameContext.forSubSystems, globalEvents).eventsAsOutcome
        v  <- processView(frameContext, m, vm)
      } yield Outcome((m, vm, v), e)
    )
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

    Outcome.join(
      for {
        m <- processModel(frameContext, model, globalEvents)
        e <- subSystemsRegister.update(frameContext.forSubSystems, globalEvents).eventsAsOutcome
      } yield Outcome((m, viewModel), e)
    )
  }

}

trait StandardFrameProcessorFunctions[StartUpData, Model, ViewModel] {
  def subSystemsRegister: SubSystemsRegister
  def eventFilters: EventFilters
  def modelUpdate: (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model]
  def viewModelUpdate: (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel]
  def viewUpdate: (FrameContext[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]

  def processModel(frameContext: FrameContext[StartUpData], model: Model, globalEvents: List[GlobalEvent]): Outcome[Model] =
    globalEvents
      .map(eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          modelUpdate(frameContext, next)(e)
        }
      }

  def processViewModel(frameContext: FrameContext[StartUpData], model: Model, viewModel: ViewModel, globalEvents: List[GlobalEvent]): Outcome[ViewModel] =
    globalEvents
      .map(eventFilters.viewModelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(viewModel)) { (acc, e) =>
        acc.flatMap { next =>
          viewModelUpdate(frameContext, model, next)(e)
        }
      }

  def processView(frameContext: FrameContext[StartUpData], model: Model, viewModel: ViewModel): Outcome[SceneUpdateFragment] =
    Outcome.merge(
      viewUpdate(frameContext, model, viewModel),
      subSystemsRegister.present(frameContext.forSubSystems)
    )(_ |+| _)
}
