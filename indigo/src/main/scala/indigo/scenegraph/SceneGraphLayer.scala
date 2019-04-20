package indigo.scenegraph

import indigo.shared.events.GlobalEvent

final case class SceneGraphLayer(nodes: List[SceneGraphNode]) extends AnyVal {

  def flatten: SceneGraphLayerFlat =
    SceneGraphLayerFlat(nodes.flatMap(_.flatten))

}

final case class SceneGraphLayerFlat(nodes: List[Renderable]) extends AnyVal {

  def collectViewEvents(gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    nodes.flatMap(n => gameEvents.flatMap(e => n.eventHandlerWithBoundsApplied(e).toList))

}
