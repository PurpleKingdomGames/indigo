package indigoexts.subsystems.automata

import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.Millis
import indigo.shared.temporal.Signal

import indigo.shared.scenegraph.SceneUpdateFragment
import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Clone

sealed trait Automaton {

  val sceneGraphNode: SceneGraphNode
  val lifespan: Millis
  val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment]
  val onCull: AutomatonSeedValues => List[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCullEvent)
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

  def apply(SceneGraphNode: SceneGraphNode, lifespan: Millis): Automaton =
    create(SceneGraphNode, lifespan, NoModifySignal, NoCullEvent)

  def create(
      sceneGraphNodeEntity: SceneGraphNode,
      lifeExpectancy: Millis,
      modifierSignal: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment],
      onCullEvent: AutomatonSeedValues => List[GlobalEvent]
  ): Automaton =
    new Automaton {
      val sceneGraphNode: SceneGraphNode                                                 = sceneGraphNodeEntity
      val lifespan: Millis                                                               = lifeExpectancy
      val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[SceneUpdateFragment] = modifierSignal
      val onCull: AutomatonSeedValues => List[GlobalEvent]                               = onCullEvent
    }

}
