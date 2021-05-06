package indigo.shared.scenegraph

import indigo.shared.BoundaryLocator
import indigo.shared.Boundary
import indigo.shared.datatypes.Rectangle
import indigo.shared.events.GlobalEvent

/**
  * Tags nodes that can handle events.
  */
trait EventHandler {
  def calculatedBounds(locator: BoundaryLocator): Boundary
  def eventHandler: ((Rectangle, GlobalEvent)) => List[GlobalEvent]
}
