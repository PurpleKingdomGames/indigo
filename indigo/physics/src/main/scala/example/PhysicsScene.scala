package example

import indigo.*
import indigo.physics.*
import indigo.scenes.*

trait PhysicsScene extends Scene[Unit, Model, Unit]:

  def world(dice: Dice): World[MyTag]

  type SceneModel     = World[MyTag]
  type SceneViewModel = Unit

  val name: SceneName

  val modelLens: Lens[Model, World[MyTag]]

  val viewModelLens: Lens[Unit, Unit] =
    Lens.keepLatest

  val eventFilters: EventFilters =
    EventFilters.Permissive

  val subSystems: Set[SubSystem[Model]] =
    Set()

  def updateModel(
      context: SceneContext[Unit],
      world: World[MyTag]
  ): GlobalEvent => Outcome[World[MyTag]] =
    case FrameTick =>
      world.update(context.frame.time.delta)

    case _ =>
      Outcome(world)

  def updateViewModel(
      context: SceneContext[Unit],
      world: World[MyTag],
      viewModel: Unit
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: SceneContext[Unit],
      world: World[MyTag],
      viewModel: Unit
  ): Outcome[SceneUpdateFragment] =
    View.present(world)
