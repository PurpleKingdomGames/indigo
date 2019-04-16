package indigoexts.subsystems.automata

import indigo.Outcome
import indigo.gameengine.scenegraph.Renderable
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.Millis
import indigo.temporal.Signal

import indigo.shared.EqualTo

final class Automaton(
    val key: AutomataPoolKey,
    val renderable: Renderable,
    val lifespan: Millis,
    val bindingKey: BindingKey,
    val modifier: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]]
) {
  def withModifier(modifier: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]]): Automaton =
    new Automaton(key, renderable, lifespan, bindingKey, modifier)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]] =
    (_, r) => Signal.fixed(Outcome(r))

  def apply(key: AutomataPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    new Automaton(key, renderable, lifespan, BindingKey.generate, NoModifySignal)

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
