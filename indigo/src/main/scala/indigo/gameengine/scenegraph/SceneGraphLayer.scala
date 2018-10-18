package indigo.gameengine.scenegraph

import indigo.gameengine.events.{GameEvent, FrameEvent}

case class SceneGraphLayer(nodes: List[SceneGraphNode]) extends AnyVal {

  def flatten: SceneGraphLayerFlat =
    SceneGraphLayerFlat(nodes.flatMap(_.flatten))

}

case class SceneGraphLayerFlat(nodes: List[Renderable]) extends AnyVal {

//  def applyAnimationMemento(animationStates: AnimationStates): SceneGraphLayerFlat =
//    this.copy(nodes = nodes.map(_.applyAnimationMemento(animationStates)))
//
//  def runAnimationActions(gameTime: GameTime): SceneGraphLayerFlat =
//    this.copy(nodes = nodes.map(_.runAnimationActions(gameTime)))

  def collectViewEvents(gameEvents: List[GameEvent]): List[FrameEvent] =
    nodes.flatMap(n => gameEvents.map(e => n.eventHandlerWithBoundsApplied(e))).collect { case Some(s) => s }

}
