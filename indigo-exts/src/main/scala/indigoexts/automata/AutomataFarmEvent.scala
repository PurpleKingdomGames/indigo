package indigoexts.automata

import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

sealed trait AutomataFarmEvent extends GlobalEvent
object AutomataFarmEvent {
  final case class Spawn(key: AutomatonPoolKey, at: Point) extends AutomataFarmEvent
  final case class KillAllInPool(key: AutomatonPoolKey)    extends AutomataFarmEvent
  final case class KillByKey(key: BindingKey)              extends AutomataFarmEvent
  case object KillAll                                      extends AutomataFarmEvent
  case object Cull                                         extends AutomataFarmEvent
}
