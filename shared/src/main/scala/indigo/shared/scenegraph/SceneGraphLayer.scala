package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent

final class SceneGraphLayer(val nodes: List[SceneGraphNode]) extends AnyVal
object SceneGraphLayer {
  val empty: SceneGraphLayer =
    new SceneGraphLayer(Nil)

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def collectViewEvents(nodes: List[SceneGraphNode], gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    nodes.flatMap {
      case group: Group =>
        collectViewEvents(group.children, gameEvents)

      case n: Renderable =>
        gameEvents.flatMap(e => n.eventHandlerWithBoundsApplied(e).toList)
    }
}
