package indigoextras.subsystems.automata

import indigo.shared.events.GlobalEvent
import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

sealed trait AutomataEvent extends GlobalEvent
object AutomataEvent {
  final case class Spawn(key: AutomataPoolKey, at: Point, lifeSpan: Option[Seconds], payload: Option[AutomatonPayload]) extends AutomataEvent
  object Spawn {
    def apply(key: AutomataPoolKey, at: Point): Spawn =
      Spawn(key, at, None, None)
  }
  case object KillAll                                  extends AutomataEvent
  case object Cull                                     extends AutomataEvent
}
