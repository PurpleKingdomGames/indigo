package indigo.gameengine.scenegraph

import indigo.gameengine.events.GlobalEvent

case class SceneGraphLayer(nodes: List[SceneGraphNode]) extends AnyVal {

  def flatten: SceneGraphLayerFlat =
    SceneGraphLayerFlat(nodes.flatMap(_.flatten))

}

case class SceneGraphLayerFlat(nodes: List[Renderable]) extends AnyVal {

  def collectViewEvents(gameEvents: List[GlobalEvent]): List[GlobalEvent] =
    nodes.flatMap(n => gameEvents.flatMap(e => n.eventHandlerWithBoundsApplied(e).toList))

}
