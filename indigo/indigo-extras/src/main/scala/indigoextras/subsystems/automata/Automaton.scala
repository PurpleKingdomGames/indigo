package indigoextras.subsystems.automata

import indigo.shared.scenegraph.{SceneGraphNode, Renderable}
import indigo.shared.temporal.Signal

import indigo.shared.events.GlobalEvent
import indigo.shared.scenegraph.Clone
import indigo.shared.time.Seconds

sealed trait Automaton {

  val sceneGraphNode: SceneGraphNode
  val lifespan: Seconds
  val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate]
  val onCull: AutomatonSeedValues => List[GlobalEvent]

  def withModifier(modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCull)

  def withOnCullEvent(onCullEvent: AutomatonSeedValues => List[GlobalEvent]): Automaton =
    Automaton.create(sceneGraphNode, lifespan, modifier, onCullEvent)
}

object Automaton {

  val NoModifySignal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] =
    (sa, n) => {
      Signal.fixed(
        n match {
          case r: Renderable =>
            AutomatonUpdate(r.moveTo(sa.spawnedAt))

          case c: Clone =>
            AutomatonUpdate(c.withTransforms(sa.spawnedAt, c.rotation, c.scale, c.alpha, c.flipHorizontal, c.flipVertical))

          case _ =>
            AutomatonUpdate(n)
        }
      )
    }

  val NoCullEvent: AutomatonSeedValues => List[GlobalEvent] =
    _ => Nil

  def apply(SceneGraphNode: SceneGraphNode, lifespan: Seconds): Automaton =
    create(SceneGraphNode, lifespan, NoModifySignal, NoCullEvent)

  def create(
      sceneGraphNodeEntity: SceneGraphNode,
      lifeExpectancy: Seconds,
      modifierSignal: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate],
      onCullEvent: AutomatonSeedValues => List[GlobalEvent]
  ): Automaton =
    new Automaton {
      val sceneGraphNode: SceneGraphNode                                             = sceneGraphNodeEntity
      val lifespan: Seconds                                                          = lifeExpectancy
      val modifier: (AutomatonSeedValues, SceneGraphNode) => Signal[AutomatonUpdate] = modifierSignal
      val onCull: AutomatonSeedValues => List[GlobalEvent]                           = onCullEvent
    }

}
