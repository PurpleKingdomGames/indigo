package indigoexts.subsystems.automata

import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

sealed trait AutomataEvent extends GlobalEvent
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point) extends AutomataEvent
  final case class KillAllInPool(key: AutomataPoolKey)    extends AutomataEvent
  final case class KillByKey(key: BindingKey)             extends AutomataEvent
  case object KillAll                                     extends AutomataEvent
  case object Cull                                        extends AutomataEvent
}
