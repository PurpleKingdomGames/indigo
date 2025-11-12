package indigo.entry

import indigo.gameengine.FrameProcessor
import indigo.scenes.SceneManager
import indigo.shared.Context
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemContext.*
import indigo.shared.subsystems.SubSystemsRegister

final class ScenesFrameProcessor[StartUpData, Model, ViewModel](
    val subSystemsRegister: SubSystemsRegister[Model],
    val sceneManager: SceneManager[StartUpData, Model, ViewModel],
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
  ): Outcome[(Model, ViewModel, SceneUpdateFragment)] = {

    val processSceneViewModel: (Model, ViewModel) => Outcome[ViewModel] = (m, vm) =>
      globalEvents
        .map(sceneManager.eventFilters.viewModelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(vm)) { (acc, e) =>
          acc.flatMap { next =>
            sceneManager.updateViewModel(context, m, next)(e)
          }
        }

    val processSceneView: (Model, ViewModel) => Outcome[SceneUpdateFragment] = (m, vm) =>
      Outcome.merge(
        processView(context, m, vm),
        sceneManager.updateView(context, m, vm)
      )(_ |+| _)

    Outcome.join(
      for {
        m   <- processModel(context, model, globalEvents)
        sm  <- processSceneModel(context, m, globalEvents)
        vm  <- processViewModel(context, sm, viewModel, globalEvents)
        svm <- processSceneViewModel(sm, vm)
        e   <- processSubSystems(context, m, globalEvents).eventsAsOutcome
        v   <- processSceneView(sm, svm)
      } yield Outcome((sm, svm, v), e)
    )
  }

  def processSceneModel(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Model] =
    globalEvents
      .map(sceneManager.eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          sceneManager.updateModel(context, next)(e)
        }
      }

  def processSubSystems(
      context: Context[StartUpData],
      model: Model,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[Unit] =
    Outcome.merge(
      subSystemsRegister.update(context.forSubSystems, model, globalEvents.toJSArray),
      sceneManager.updateSubSystems(context.forSubSystems, model, globalEvents)
    )((_, _) => ())
