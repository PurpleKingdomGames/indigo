package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent

/**
  * Tags nodes that can handle events.
  */
trait EventHandler {
  def calculatedBounds(locator: BoundaryLocator): Rectangle
  def eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
}
