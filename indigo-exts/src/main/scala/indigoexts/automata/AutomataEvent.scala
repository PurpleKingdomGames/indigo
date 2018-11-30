package indigoexts.automata

import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.datatypes.{BindingKey, Point}

sealed trait AutomataEvent extends GlobalEvent
object AutomataEvent {
  case class Spawn(key: AutomataPoolKey, at: Point)                                                           extends AutomataEvent
  case class ModifyAndSpawn(key: AutomataPoolKey, at: Point, modifier: PartialFunction[Automaton, Automaton]) extends AutomataEvent
  case class KillAllInPool(key: AutomataPoolKey)                                                              extends AutomataEvent
  case class KillByKey(key: BindingKey)                                                                       extends AutomataEvent
  case object KillAll                                                                                         extends AutomataEvent
  case object Cull                                                                                            extends AutomataEvent
}
