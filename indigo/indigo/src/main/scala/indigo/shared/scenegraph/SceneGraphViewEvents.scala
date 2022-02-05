package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent

import scala.scalajs.js.JSConverters._

object SceneGraphViewEvents:

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  private def pushEvents(outputEvents: List[GlobalEvent], sendEvent: GlobalEvent => Unit): Unit = {
    val count = outputEvents.length
    var index = 0

    while (index < count) {
      sendEvent(outputEvents(index))
      index += 1
    }
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
  def applyInputEvents(
      node: EventHandler,
      boundary: Option[Rectangle],
      inputEvents: scalajs.js.Array[GlobalEvent],
      sendEvent: GlobalEvent => Unit
  ): Unit =
    boundary match
      case Some(bounds) =>
        val count = inputEvents.length
        var index = 0

        while (index < count) {
          pushEvents(node.eventHandler((bounds, inputEvents(index))), sendEvent)
          index += 1
        }

      case _ =>
        ()
