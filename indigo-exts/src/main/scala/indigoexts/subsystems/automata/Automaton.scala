package indigoexts.subsystems.automata

import indigo.shared.scenegraph.Renderable
import indigo.Millis
import indigo.shared.temporal.Signal

import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.GlobalEvent

sealed trait Automaton {

  type PayloadType

  val key: AutomataPoolKey
  val renderable: Renderable
  val lifespan: Millis
  val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]
  val onCull: AutomatonSeedValues => List[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment]): Automaton =
    Automaton.create(key, renderable, lifespan, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    Automaton.create(key, renderable, lifespan, modifier, onCullEvent)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] =
    (sa, r) => {
      Signal.fixed(
        SceneUpdateFragment.empty
          .addGameLayerNodes(r.moveTo(sa.spawnedAt))
      )
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(key: AutomataPoolKey, renderable: Renderable, lifespan: Millis): Automaton =
    create(key, renderable, lifespan, NoModifySignal, NoCullEvent)

  def create(
      poolKey: AutomataPoolKey,
      renderableEntity: Renderable,
      lifeExpectancy: Millis,
      modifierSignal: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment],
      onCullEvent: AutomatonSeedValues => List[GlobalEvent]
  ): Automaton =
    new Automaton {
      val key: AutomataPoolKey                                                       = poolKey
      val renderable: Renderable                                                     = renderableEntity
      val lifespan: Millis                                                           = lifeExpectancy
      val modifier: (AutomatonSeedValues, Renderable) => Signal[SceneUpdateFragment] = modifierSignal
      val onCull: AutomatonSeedValues => List[GlobalEvent]                           = onCullEvent
    }

}
