package indigo.entry

import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.Context
import indigo.shared.FontRegister
import indigo.shared.Outcome
import indigo.shared.collections.Batch
import indigo.shared.datatypes.RGBA
import indigo.shared.events.EventFilters
import indigo.shared.events.GlobalEvent
import indigo.shared.materials.BlendMaterial
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.subsystems.SubSystemsRegister

class StandardFrameProcessorTests extends munit.FunSuite {

  import TestFixtures._

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, new FontRegister)

  test("standard frame processor") {

    val outcome = standardFrameProcessor.run(
      model,
      viewModel,
      Batch(EventsOnlyEvent.Increment),
      Context.initial
    )

    val outModel     = outcome.unsafeGet._1
    val outViewModel = outcome.unsafeGet._2
    val outView      = outcome.unsafeGet._3

    @SuppressWarnings(Array("scalafix:DisableSyntax.throw"))
    val ambientLight: RGBA =
      outView.blendMaterial match {
        case Some(BlendMaterial.Lighting(rgba)) =>
          rgba
        case _ =>
          throw new Exception("Boom! Missing blend material.")
      }

    assert(outModel.count == 1)
    assert(outViewModel == 10)
    assert(ambientLight == RGBA.Red.withAlpha(0.5))
    assert(outcome.unsafeGlobalEvents.length == 2)
    assert(outcome.unsafeGlobalEvents.contains(EventsOnlyEvent.Increment))
    assert(outcome.unsafeGlobalEvents.contains(EventsOnlyEvent.Total(1)))
    assert(outcome.unsafeGlobalEvents == Batch(EventsOnlyEvent.Total(1), EventsOnlyEvent.Increment))

  }

}

object TestFixtures {

  val model =
    GameModel(0)

  val viewModel: Int =
    0

  val modelUpdate: (Context[Unit], GameModel) => GlobalEvent => Outcome[GameModel] =
    (_, m) => {
      case EventsOnlyEvent.Increment =>
        val newCount = m.count + 1
        Outcome(m.copy(count = newCount)).addGlobalEvents(EventsOnlyEvent.Total(newCount))

      case EventsOnlyEvent.Decrement =>
        Outcome(m)

      case _ =>
        Outcome(m)
    }

  val viewModelUpdate: (Context[Unit], GameModel, Int) => GlobalEvent => Outcome[Int] =
    (_, _, vm) => _ => Outcome(vm + 10).addGlobalEvents(EventsOnlyEvent.Increment)

  val viewUpdate: (Context[Unit], GameModel, Int) => Outcome[SceneUpdateFragment] =
    (_, _, _) => Outcome(SceneUpdateFragment.empty.withBlendMaterial(BlendMaterial.Lighting(RGBA.Red.withAlpha(0.5))))

  val standardFrameProcessor: StandardFrameProcessor[Unit, GameModel, Int] =
    new StandardFrameProcessor(
      new SubSystemsRegister(),
      EventFilters.AllowAll,
      modelUpdate,
      viewModelUpdate,
      viewUpdate
    )

  final case class GameModel(count: Int)

  sealed trait EventsOnlyEvent extends GlobalEvent
  object EventsOnlyEvent {
    case object Increment              extends EventsOnlyEvent
    case object Decrement              extends EventsOnlyEvent
    final case class Total(count: Int) extends EventsOnlyEvent
  }
}
