package indigoexts.subsystems.automata

import indigo.shared.scenegraph.Renderable
import indigo.shared.datatypes.BindingKey
import indigo.Millis
import indigo.shared.temporal.Signal

import indigo.shared.EqualTo
import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.GlobalEvent

sealed trait Automaton {

  type PayloadType

  val key: AutomataPoolKey
  val renderable: Renderable
  val lifespan: Millis
  val bindingKey: BindingKey
  val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]
  val onCull: AutomatonSeedValues => Option[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]): Automaton =
    Automaton.create(key, renderable, lifespan, bindingKey, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => Option[GlobalEvent]): Automaton =
    Automaton.create(key, renderable, lifespan, bindingKey, modifier, onCullEvent)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
    (_, r) => Signal.fixed(SceneUpdateFragment.empty.addGameLayerNodes(r))

  val NoCullEvent: AutomatonSeedValues => Option[GlobalEvent] =
    _ => None

  def apply(key: AutomataPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    create(key, renderable, lifespan, BindingKey.generate, NoModifySignal, NoCullEvent)

  def create(
      poolKey: AutomataPoolKey,
      renderableEntity: Renderable,
      lifeExpectancy: Millis,
      bindingKeyValue: BindingKey,
      modifierSignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment],
      onCullEvent: AutomatonSeedValues => Option[GlobalEvent]
  ): Automaton =
    new Automaton {
      val key: AutomataPoolKey                                                       = poolKey
      val renderable: Renderable                                                     = renderableEntity
      val lifespan: Millis                                                           = lifeExpectancy
      val bindingKey: BindingKey                                                     = bindingKeyValue
      val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] = modifierSignal
      val onCull: AutomatonSeedValues => Option[GlobalEvent]                         = onCullEvent
    }

}

final class AutomataPoolKey(val key: String) extends AnyVal
object AutomataPoolKey {

  implicit val eq: EqualTo[AutomataPoolKey] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[String]].equal(a.key, b.key)
    }

  def apply(key: String): AutomataPoolKey =
    new AutomataPoolKey(key)

  def generate: AutomataPoolKey =
    AutomataPoolKey(BindingKey.generate.value)

}
