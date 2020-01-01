package indigo.shared.scenegraph

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.Rectangle

object SceneGraphViewEvents {

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var"))
  def pushEvents(outputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = outputEvents.length
    var index = 0

    while (index < count) {
      sendEvent(outputEvents(index))
      index += 1
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var"))
  def applyInputEvents(node: EventHandling, bounds: Rectangle, inputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = inputEvents.length
    var index = 0

    while (index < count) {
      pushEvents(node.eventHandler((bounds, inputEvents(index))), sendEvent)
      index += 1
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.While", "org.wartremover.warts.Var", "org.wartremover.warts.Recursion"))
  def collectViewEvents(nodes: List[SceneGraphNode], inputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = nodes.length
    var index = 0

    while (index < count) {
      nodes(index) match {
        case s: Sprite =>
          applyInputEvents(s, s.bounds, inputEvents, sendEvent)

        case t: Text =>
          applyInputEvents(t, t.bounds, inputEvents, sendEvent)

        case _: Graphic =>
          ()

        case g: Group =>
          collectViewEvents(g.children, inputEvents, sendEvent)

        case _: Clone =>
          ()

        case _: CloneBatch =>
          ()
      }

      index += 1
    }
  }

}
