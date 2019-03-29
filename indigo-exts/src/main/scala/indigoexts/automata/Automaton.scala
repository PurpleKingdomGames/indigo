package indigoexts.automata

import indigo.Outcome
import indigo.gameengine.scenegraph.Renderable
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.GameTime.Millis
import indigoexts.temporal.Signal

import indigo.EqualTo._

final class Automaton(val key: AutomataPoolKey, val renderable: Renderable, val lifespan: Millis, val bindingKey: BindingKey, val modifier: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]]) {
  def withModifier(modifier: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]]): Automaton =
    new Automaton(key, renderable, lifespan, bindingKey, modifier)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, Renderable) => Signal[Outcome[Renderable]] =
    (_, r) => Signal.fixed(Outcome(r))

  def apply(key: AutomataPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    new Automaton(key, renderable, lifespan, BindingKey.generate, NoModifySignal)

}

final case class AutomataPoolKey(key: String) extends AnyVal {
  def ===(other: AutomataPoolKey): Boolean =
    AutomataPoolKey.equality(this, other)
}
object AutomataPoolKey {
  def generate: AutomataPoolKey =
    AutomataPoolKey(BindingKey.generate.value)

  def equality(a: AutomataPoolKey, b: AutomataPoolKey): Boolean =
    a.key === b.key
}
