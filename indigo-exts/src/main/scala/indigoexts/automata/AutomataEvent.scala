package indigoexts.automata

import indigo.gameengine.events.FrameEvent
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

sealed trait AutomataEvent extends FrameEvent
object AutomataEvent {
  case class Spawn(key: AutomataPoolKey, at: Point) extends AutomataEvent
  case class KillAllInPool(key: AutomataPoolKey)    extends AutomataEvent
  case class KillByKey(key: BindingKey)             extends AutomataEvent
  case object KillAll                               extends AutomataEvent
}
