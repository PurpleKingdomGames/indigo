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

// @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
final class ScenesFrameProcessor[StartUpData, Model, ViewModel](
    subSystemsRegister: SubSystemsRegister,
    sceneManager: SceneManager[StartUpData, Model, ViewModel]
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
        .map(sceneManager.eventFilters.modelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMap { next =>
            sceneManager.updateModel(frameContext, next)(e)
          }
        }

    val subSystemEvents: Outcome[Unit] =
      Outcome.merge(
        subSystemsRegister.update(frameContext.forSubSystems, globalEvents),
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)
      )((_, _) => ())

    val updatedViewModel: Model => Outcome[ViewModel] = m =>
      globalEvents
        .map(sceneManager.eventFilters.viewModelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(viewModel)) { (acc, e) =>
          acc.flatMap { next =>
            sceneManager.updateViewModel(frameContext, m, next)(e)
          }
        }

    val view: Model => ViewModel => Outcome[SceneUpdateFragment] = m =>
      vm =>
        Outcome.merge(
          sceneManager.updateView(frameContext, m, vm),
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
        .map(sceneManager.eventFilters.modelFilter)
        .collect { case Some(e) => e }
        .foldLeft(Outcome(model)) { (acc, e) =>
          acc.flatMap { next =>
            sceneManager.updateModel(frameContext, next)(e)
          }
        }

    val subSystemEvents: Outcome[Unit] =
      Outcome.merge(
        subSystemsRegister.update(frameContext.forSubSystems, globalEvents),
        sceneManager.updateSubSystems(frameContext.forSubSystems, globalEvents)
      )((_, _) => ())

    Outcome.join(
      for {
        m <- updatedModel
        e <- subSystemEvents.eventsAsOutcome
      } yield Outcome((m, viewModel), e)
    )
  }
}
