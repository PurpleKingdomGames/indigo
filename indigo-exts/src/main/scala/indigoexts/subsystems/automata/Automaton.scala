package indigoexts.subsystems.automata

import indigo.shared.scenegraph.Renderable
import indigo.shared.datatypes.BindingKey
import indigo.Millis
import indigo.shared.temporal.Signal

import indigo.shared.EqualTo
import indigo.shared.scenegraph.SceneUpdateFragment

sealed trait Automaton {

  type PayloadType

  val key: AutomataPoolKey
  val renderable: Renderable
  val lifespan: Millis
  val bindingKey: BindingKey
  val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]

  def withModifier(modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]): Automaton =
    Automaton.createWithModififer(key, renderable, lifespan, bindingKey, modifier)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
    (_, r) => Signal.fixed(SceneUpdateFragment.empty.addGameLayerNodes(r))

  def apply(key: AutomataPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    createWithModififer(key, renderable, lifespan, BindingKey.generate, NoModifySignal)

  def createWithModififer(
      poolKey: AutomataPoolKey,
      renderableEntity: Renderable,
      lifeExpectancy: Millis,
      bindingKeyValue: BindingKey,
      modifierSignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]
  ): Automaton =
    new Automaton {
      val key: AutomataPoolKey                                                       = poolKey
      val renderable: Renderable                                                     = renderableEntity
      val lifespan: Millis                                                           = lifeExpectancy
      val bindingKey: BindingKey                                                     = bindingKeyValue
      val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] = modifierSignal
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
