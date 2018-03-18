package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.Point

sealed trait AutomataEvent extends ViewEvent
object AutomataEvent {
  case class Spawn(key: AutomataKey, at: Point) extends AutomataEvent
  case class KillByKey(key: AutomataKey) extends AutomataEvent
  case object KillAll extends AutomataEvent
}
