package indigo.scenes

import indigo.shared.FrameContext
import indigo.shared.IndigoLogger
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.collections.NonEmptyList
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.subsystems.SubSystemFrameContext._
import indigo.shared.subsystems.SubSystemsRegister
import indigo.shared.time.Seconds

class SceneManager[StartUpData, GameModel, ViewModel](
    scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]],
    scenesFinder: SceneFinder
):

  private given CanEqual[Option[Scene[StartUpData, GameModel, ViewModel]], Option[
    Scene[StartUpData, GameModel, ViewModel]
  ]] =
    CanEqual.derived

  // Scene management
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var finderInstance: SceneFinder = scenesFinder
  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  private var lastSceneChangeAt: Seconds = Seconds.zero

  private val subSystemStates: scalajs.js.Dictionary[SubSystemsRegister[GameModel]] =
    scalajs.js.Dictionary
      .empty[SubSystemsRegister[GameModel]]
      .addAll(
        scenes.toList.map { s =>
          val r = new SubSystemsRegister[GameModel]()
          r.register(Batch.fromSet(s.subSystems))
          (s.name.toString -> r)
        }
      )

  // Scene delegation

  def updateModel(frameContext: FrameContext[StartUpData], model: GameModel): GlobalEvent => Outcome[GameModel] =
    case SceneEvent.First =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.first
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Last =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.last
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Next =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.forward
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.LoopNext =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.forwardLoop
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.Previous =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.backward
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.LoopPrevious =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.backwardLoop
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case SceneEvent.JumpTo(name) =>
      lastSceneChangeAt = frameContext.gameTime.running

      val from = finderInstance.current.name
      finderInstance = finderInstance.jumpToSceneByName(name)
      val to = finderInstance.current.name

      val events =
        if from == to then Batch.empty
        else Batch(SceneEvent.SceneChange(from, to, lastSceneChangeAt))

      Outcome(model, events)

    case event =>
      scenes.find(_.name == finderInstance.current.name) match
        case None =>
          IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name)
          Outcome(model)

        case Some(scene) =>
          Scene
            .updateModel(
              scene,
              SceneContext(scene.name, lastSceneChangeAt, frameContext),
              model
            )(event)

  def updateSubSystems(
      frameContext: SubSystemFrameContext[Unit],
      model: GameModel,
      globalEvents: Batch[GlobalEvent]
  ): Outcome[SubSystemsRegister[GameModel]] =
    scenes
      .find(_.name == finderInstance.current.name)
      .flatMap { scene =>
        subSystemStates
          .get(scene.name.toString)
          .map {
            _.update(frameContext, model, globalEvents.toJSArray)
          }
      }
      .getOrElse(
        Outcome.raiseError(
          new Exception(s"Couldn't find scene with name '${finderInstance.current.name}' in order to update subsystems")
        )
      )

  def updateViewModel(
      frameContext: FrameContext[StartUpData],
      model: GameModel,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    scenes.find(_.name == finderInstance.current.name) match
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name)
        _ => Outcome(viewModel)

      case Some(scene) =>
        Scene.updateViewModel(
          scene,
          SceneContext(scene.name, lastSceneChangeAt, frameContext),
          model,
          viewModel
        )

  def updateView(
      frameContext: FrameContext[StartUpData],
      model: GameModel,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] =
    scenes.find(_.name == finderInstance.current.name) match
      case None =>
        IndigoLogger.errorOnce("Could not find scene called: " + finderInstance.current.name)
        Outcome(SceneUpdateFragment.empty)

      case Some(scene) =>
        val subsystemView = subSystemStates
          .get(scene.name.toString)
          .map { ssr =>
            ssr.present(frameContext.forSubSystems, model)
          }
          .getOrElse(Outcome(SceneUpdateFragment.empty))

        Outcome.merge(
          Scene.updateView(
            scene,
            SceneContext(scene.name, lastSceneChangeAt, frameContext),
            model,
            viewModel
          ),
          subsystemView
        )(_ |+| _)

  def eventFilters: EventFilters =
    scenes.find(_.name == finderInstance.current.name) match
      case None =>
        // This should never be the case, we should always find a scene.
        EventFilters((_: GlobalEvent) => None, (_: GlobalEvent) => None)

      case Some(value) =>
        value.eventFilters

object SceneManager:

  def apply[StartUpData, GameModel, ViewModel](
      scenes: NonEmptyList[Scene[StartUpData, GameModel, ViewModel]],
      initialScene: SceneName
  ): SceneManager[StartUpData, GameModel, ViewModel] =
    new SceneManager[StartUpData, GameModel, ViewModel](
      scenes,
      SceneFinder.fromScenes[StartUpData, GameModel, ViewModel](scenes).jumpToSceneByName(initialScene)
    )
