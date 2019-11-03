package indigoexts.subsystems.automata

import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.events.GlobalEvent

final class AutomatonUpdate(val nodes: List[SceneGraphNode], val events: List[GlobalEvent]) {

  def |+|(other: AutomatonUpdate): AutomatonUpdate =
    AutomatonUpdate(nodes ++ other.nodes, events ++ other.events)

}

object AutomatonUpdate {

  def empty: AutomatonUpdate =
    new AutomatonUpdate(Nil, Nil)

  def apply(nodes: List[SceneGraphNode], events: List[GlobalEvent]): AutomatonUpdate =
    new AutomatonUpdate(nodes, events)

  def withNodes(nodes: SceneGraphNode*): AutomatonUpdate =
    new AutomatonUpdate(nodes.toList, Nil)

}
