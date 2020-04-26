package indigoexts.subsystems.automata

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

sealed trait AutomataEvent extends GlobalEvent
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point, lifeSpan: Option[Seconds], payload: Option[AutomatonPayload]) extends AutomataEvent
  final case class KillAllInPool(key: AutomataPoolKey)                                                                 extends AutomataEvent
  case object KillAll                                                                                                  extends AutomataEvent
  case object Cull                                                                                                     extends AutomataEvent
}
