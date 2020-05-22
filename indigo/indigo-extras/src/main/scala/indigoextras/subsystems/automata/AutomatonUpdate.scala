package indigoextras.subsystems.automata

import indigo.shared.scenegraph.SceneGraphNode
import indigo.shared.events.GlobalEvent

final class AutomatonUpdate(val nodes: List[SceneGraphNode], val events: List[GlobalEvent]) {

  def |+|(other: AutomatonUpdate): AutomatonUpdate =
    AutomatonUpdate(nodes ++ other.nodes, events ++ other.events)

  def addGlobalEvents(newEvents: GlobalEvent*): AutomatonUpdate =
    addGlobalEvents(newEvents.toList)

  def addGlobalEvents(newEvents: List[GlobalEvent]): AutomatonUpdate =
    new AutomatonUpdate(nodes, events ++ newEvents)

}

object AutomatonUpdate {

  def empty: AutomatonUpdate =
    new AutomatonUpdate(Nil, Nil)

  def apply(nodes: List[SceneGraphNode], events: List[GlobalEvent]): AutomatonUpdate =
    new AutomatonUpdate(nodes, events)

  def apply(nodes: SceneGraphNode*): AutomatonUpdate =
    new AutomatonUpdate(nodes.toList, Nil)

  def apply(nodes: List[SceneGraphNode]): AutomatonUpdate =
    new AutomatonUpdate(nodes, Nil)

}
