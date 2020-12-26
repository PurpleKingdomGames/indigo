package indigo.entry

import indigo.shared.time.GameTime
import indigo.shared.events.InputState
import indigo.shared.dice.Dice
import indigo.shared.events.GlobalEvent
import indigo.shared.Outcome
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister
import indigo.shared.FontRegister
import indigo.shared.FrameContext
import indigo.shared.events.EventFilters
import indigo.shared.subsystems.SubSystemsRegister

class StandardFrameProcessorTests extends munit.FunSuite {

  import TestFixtures._

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, new FontRegister)

  test("standard frame processor") {

    val outcome = standardFrameProcessor.run(
      (),
      model,
      viewModel,
      GameTime.zero,
      List(EventsOnlyEvent.Increment),
      InputState.default,
      Dice.loaded(0),
      boundaryLocator
    )

    val outModel     = outcome.unsafeGet._1
    val outViewModel = outcome.unsafeGet._2
    val outView      = outcome.unsafeGet._3

    assert(outModel.count == 1)
    assert(outViewModel == 10)
    assert(outView.ambientLight.a == 0.5d)
    assert(outcome.unsafeGlobalEvents.length == 2)
    assert(outcome.unsafeGlobalEvents.contains(EventsOnlyEvent.Increment))
    assert(outcome.unsafeGlobalEvents.contains(EventsOnlyEvent.Total(1)))
    assert(outcome.unsafeGlobalEvents == List(EventsOnlyEvent.Total(1), EventsOnlyEvent.Increment))

  }

}

object TestFixtures {

  val model =
    GameModel(0)

  val viewModel: Int =
    0

  val modelUpdate: (FrameContext[Unit], GameModel) => GlobalEvent => Outcome[GameModel] =
    (_, m) => {
      case EventsOnlyEvent.Increment =>
        val newCount = m.count + 1
        Outcome(m.copy(count = newCount)).addGlobalEvents(EventsOnlyEvent.Total(newCount))

      case EventsOnlyEvent.Decrement =>
        Outcome(m)

      case _ =>
        Outcome(m)
    }

  val viewModelUpdate: (FrameContext[Unit], GameModel, Int) => GlobalEvent => Outcome[Int] =
    (_, _, vm) =>
      _ => {
        Outcome(vm + 10).addGlobalEvents(EventsOnlyEvent.Increment)
      }

  val viewUpdate: (FrameContext[Unit], GameModel, Int) => Outcome[SceneUpdateFragment] =
    (_, _, _) => Outcome(SceneUpdateFragment.empty.withAmbientLightAmount(0.5))

  val standardFrameProcessor: StandardFrameProcessor[Unit, GameModel, Int] = {
    new StandardFrameProcessor(new SubSystemsRegister(), EventFilters.AllowAll, modelUpdate, viewModelUpdate, viewUpdate)
  }

  final case class GameModel(count: Int)

  sealed trait EventsOnlyEvent extends GlobalEvent
  object EventsOnlyEvent {
    case object Increment              extends EventsOnlyEvent
    case object Decrement              extends EventsOnlyEvent
    final case class Total(count: Int) extends EventsOnlyEvent
  }
}
