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
final class StandardFrameProcessor[StartUpData, Model, ViewModel](
    subSystemsRegister: SubSystemsRegister,
    eventFilters: EventFilters,
    modelUpdate: (FrameContext[StartUpData], Model) => GlobalEvent => Outcome[Model],
    viewModelUpdate: (FrameContext[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    viewUpdate: (FrameContext[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]
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
        .map(eventFilters.modelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMap { next =>
            modelUpdate(frameContext, next)(e)
          }
        }

    val subSystemEvents: Outcome[SubSystemsRegister] =
      subSystemsRegister.update(frameContext.forSubSystems, globalEvents)

    val updatedViewModel: Model => Outcome[ViewModel] = m =>
      globalEvents
        .map(eventFilters.viewModelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(viewModel)) { (acc, e) =>
          acc.flatMap { next =>
            viewModelUpdate(frameContext, m, next)(e)
          }
        }

    val view: Model => ViewModel => Outcome[SceneUpdateFragment] = m =>
      vm =>
        Outcome.merge(
          viewUpdate(frameContext, m, vm),
          subSystemsRegister.present(frameContext.forSubSystems)
        )(_ |+| _)

    Outcome.join(
      for {
        m  <- updatedModel
        vm <- updatedViewModel(m)
        e  <- subSystemEvents.eventsAsOutcome
        v  <- view(m)(vm)
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

    val updatedModel: Outcome[Model] =
      globalEvents
        .map(eventFilters.modelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMap { next =>
            modelUpdate(frameContext, next)(e)
          }
        }

    val subSystemEvents: Outcome[SubSystemsRegister] =
      subSystemsRegister.update(frameContext.forSubSystems, globalEvents)

    Outcome.join(
      for {
        m <- updatedModel
        e <- subSystemEvents.eventsAsOutcome
      } yield Outcome((m, viewModel), e)
    )
  }
}
