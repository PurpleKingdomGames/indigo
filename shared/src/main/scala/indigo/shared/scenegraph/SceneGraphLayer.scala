package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent

final class SceneGraphLayer(val nodes: List[SceneGraphNode]) extends AnyVal {

  def flatten: SceneGraphLayerFlat =
    new SceneGraphLayerFlat(nodes.flatMap(_.flatten))

}
object SceneGraphLayer {
  val empty: SceneGraphLayer =
    new SceneGraphLayer(Nil)
}

final class SceneGraphLayerFlat(val nodes: List[Renderable]) extends AnyVal {

  def collectViewEvents(gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    nodes.flatMap(n => gameEvents.flatMap(e => n.eventHandlerWithBoundsApplied(e).toList))

}
