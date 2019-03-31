package indigoexts.automata

import indigo.Outcome
import indigo.gameengine.scenegraph.Renderable
import indigo.gameengine.scenegraph.datatypes.BindingKey
import indigo.Millis
import indigoexts.temporal.Signal

final class Automaton(
    val key: AutomatonPoolKey,
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

  def apply(key: AutomatonPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    new Automaton(key, renderable, lifespan, BindingKey.generate, NoModifySignal)

}
