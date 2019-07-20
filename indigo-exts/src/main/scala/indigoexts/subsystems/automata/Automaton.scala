package indigoexts.subsystems.automata

import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.Millis
import indigo.shared.temporal.Signal

import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Clone

sealed trait Automaton {

  type PayloadType

  val key: AutomataPoolKey
  val sceneGraphNode: SceneGraphNode
  val lifespan: Millis
  val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment]
  val onCull: AutomatonSeedValues => List[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment]): Automaton =
    Automaton.create(key, sceneGraphNode, lifespan, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    Automaton.create(key, sceneGraphNode, lifespan, modifier, onCullEvent)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment] =
    (sa, n) => {
      Signal.fixed(
        SceneUpdateFragment.empty
          .addGameLayerNodes {
            n match {
              case r: Renderable =>
                r.moveTo(sa.spawnedAt)

              case c: Clone =>
                c.withTransforms(sa.spawnedAt, c.rotation, c.scale)

              case _ =>
                n
            }

          }
      )
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(key: AutomataPoolKey, SceneGraphNode: SceneGraphNode, lifespan: Millis): Automaton =
    create(key, SceneGraphNode, lifespan, NoModifySignal, NoCullEvent)

  def create(
      poolKey: AutomataPoolKey,
      sceneGraphNodeEntity: SceneGraphNode,
      lifeExpectancy: Millis,
      modifierSignal: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment],
      onCullEvent: AutomatonSeedValues => List[GlobalEvent]
  ): Automaton =
    new Automaton {
      val key: AutomataPoolKey                                                           = poolKey
      val sceneGraphNode: SceneGraphNode                                                 = sceneGraphNodeEntity
      val lifespan: Millis                                                               = lifeExpectancy
      val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment] = modifierSignal
      val onCull: AutomatonSeedValues => List[GlobalEvent]                               = onCullEvent
    }

}
