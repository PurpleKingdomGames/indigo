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
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.scenes.SceneManager
import indigo.shared.events.EventFilters

final case class ScenesFrameProcessor[StartUpData, Model, ViewModel](
    subSystemsRegister: SubSystemsRegister,
    sceneManager: SceneManager[StartUpData, Model, ViewModel],
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

    val subSystemEvents: Outcome[Unit] =
      Outcome.merge(
        subSystemsRegister.update(frameContext.forSubSystems, globalEvents),
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)
      )((_, _) => ())

    val processSceneViewModel: (Model, ViewModel) => Outcome[ViewModel] = (m, vm) =>
      globalEvents
        .map(sceneManager.eventFilters.viewModelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(vm)) { (acc, e) =>
          acc.flatMap { next =>
            sceneManager.updateViewModel(frameContext, m, next)(e)
          }
        }

    val processSceneView: (Model, ViewModel) => Outcome[SceneUpdateFragment] = (m, vm) =>
      Outcome.merge(
        processView(frameContext, m, vm),
        sceneManager.updateView(frameContext, m, vm)
      )(_ |+| _)

    Outcome.join(
      for {
        m   <- processModel(frameContext, model, globalEvents)
        sm  <- processSceneModel(frameContext, m, globalEvents)
        vm  <- processViewModel(frameContext, sm, viewModel, globalEvents)
        svm <- processSceneViewModel(sm, vm)
        e   <- subSystemEvents.eventsAsOutcome
        v   <- processSceneView(sm, svm)
      } yield Outcome((sm, svm, v), e)
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

    val subSystemEvents: Outcome[Unit] =
      Outcome.merge(
        subSystemsRegister.update(frameContext.forSubSystems, globalEvents),
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)
      )((_, _) => ())

    Outcome.join(
      for {
        m  <- processModel(frameContext, model, globalEvents)
        sm <- processSceneModel(frameContext, m, globalEvents)
        e  <- subSystemEvents.eventsAsOutcome
      } yield Outcome((sm, viewModel), e)
    )
  }

  def processSceneModel(frameContext: FrameContext[StartUpData], model: Model, globalEvents: List[GlobalEvent]): Outcome[Model] =
    globalEvents
      .map(sceneManager.eventFilters.modelFilter)
      .collect { case Some(e) => e }
      .foldLeft(Outcome(model)) { (acc, e) =>
        acc.flatMap { next =>
          sceneManager.updateModel(frameContext, next)(e)
        }
      }
}
