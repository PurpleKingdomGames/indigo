package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.Rectangle
import indigo.shared.BoundaryLocator

object SceneGraphViewEvents {

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def pushEvents(outputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = outputEvents.length
    var index = 0

    while (index < count) {
      sendEvent(outputEvents(index))
      index += 1
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def applyInputEvents(node: EventHandler, bounds: Rectangle, inputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = inputEvents.length
    var index = 0

    while (index < count) {
      pushEvents(node.eventHandler((bounds, inputEvents(index))), sendEvent)
      index += 1
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var"))
  def collectViewEvents(boundaryLocator: BoundaryLocator, nodes: List[SceneGraphNode], inputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = nodes.length
    var index = 0

    while (index < count) {
      nodes(index) match {
        case g: Group =>
          collectViewEvents(boundaryLocator, g.children, inputEvents, sendEvent)

        case t: EventHandler =>
          applyInputEvents(t, t.bounds(boundaryLocator), inputEvents, sendEvent)

        case _ =>
          ()
      }

      index += 1
    }
  }

}
