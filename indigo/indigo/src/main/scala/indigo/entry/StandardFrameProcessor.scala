package indigo.entry

import indigo.gameengine.FrameProcessor
import indigo.platform.renderer.Renderer
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.dice.Dice
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.events.InputState
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemContext._
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.time.GameTime

final class StandardFrameProcessor[StartUpData, Model, ViewModel](
    val subSystemsRegister: SubSystemsRegister[Model],
    val eventFilters: EventFilters,
    val modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model],
    val viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    val viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model, ViewModel]
    with StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:

  def run(
      startUpData: => StartUpData,
      model: => Model,
      viewModel: => ViewModel,
      gameTime: GameTime,
      globalEvents: Batch[GlobalEvent],
      inputState: InputState,
      dice: Dice,
      boundaryLocator: BoundaryLocator,
      renderer: => Renderer
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)] =
    val frameContext =
      Context[StartUpData](gameTime, dice, inputState, boundaryLocator, startUpData, renderer.captureScreen)

    Outcome.join(
      for {
        m  <- processModel(frameContext, model, globalEvents)
        vm <- processViewModel(frameContext, m, viewModel, globalEvents)
        e  <- subSystemsRegister.update(frameContext.forSubSystems, m, globalEvents.toJSArray).eventsAsOutcome
        v  <- processView(frameContext, m, vm)
      } yield Outcome((m, vm, v), e)
    )

trait StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:
  def subSystemsRegister: SubSystemsRegister[Model]
  def eventFilters: EventFilters
  def modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model]
  def viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel]
  def viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]

  def processModel(
      frameContext: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Model] =
    globalEvents
      .map(eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          modelUpdate(frameContext, next)(e)
        }
      }

  def processViewModel(
      frameContext: Context[StartUpData],
      model: Model,
      viewModel: ViewModel,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[ViewModel] =
    globalEvents
      .map(eventFilters.viewModelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(viewModel)) { (acc, e) =>
        acc.flatMap { next =>
          viewModelUpdate(frameContext, model, next)(e)
        }
      }

  def processView(
      frameContext: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome.merge(
      viewUpdate(frameContext, model, viewModel),
      subSystemsRegister.present(frameContext.forSubSystems, model)
    )(_ |+| _)
