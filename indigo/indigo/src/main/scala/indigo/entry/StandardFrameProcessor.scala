package indigo.entry

import indigo.gameengine.FrameProcessor
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister

final class StandardFrameProcessor[StartUpData, Model, ViewModel](
    val subSystemsRegister: SubSystemsRegister[Model],
    val eventFilters: EventFilters,
    val modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model],
    val viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel],
    val viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]
) extends FrameProcessor[StartUpData, Model, ViewModel]
    with StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:

  def run(
      model: => Model,
      viewModel: => ViewModel,
      globalEvents: Batch[GlobalEvent],
      context: => Context[StartUpData]
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)] =
    Outcome.join(
      for {
        m  <- processModel(context, model, globalEvents)
        vm <- processViewModel(context, m, viewModel, globalEvents)
        e  <- subSystemsRegister.update(context.forSubSystems, m, globalEvents.toJSArray).eventsAsOutcome
        v  <- processView(context, m, vm)
      } yield Outcome((m, vm, v), e)
    )

trait StandardFrameProcessorFunctions[StartUpData, Model, ViewModel]:
  def subSystemsRegister: SubSystemsRegister[Model]
  def eventFilters: EventFilters
  def modelUpdate: (Context[StartUpData], Model) => GlobalEvent => Outcome[Model]
  def viewModelUpdate: (Context[StartUpData], Model, ViewModel) => GlobalEvent => Outcome[ViewModel]
  def viewUpdate: (Context[StartUpData], Model, ViewModel) => Outcome[SceneUpdateFragment]

  def processModel(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Model] =
    globalEvents
      .map(eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          modelUpdate(context, next)(e)
        }
      }

  def processViewModel(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[ViewModel] =
    globalEvents
      .map(eventFilters.viewModelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(viewModel)) { (acc, e) =>
        acc.flatMap { next =>
          viewModelUpdate(context, model, next)(e)
        }
      }

  def processView(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome.merge(
      viewUpdate(context, model, viewModel),
      subSystemsRegister.present(context.forSubSystems, model)
    )(_ |+| _)
