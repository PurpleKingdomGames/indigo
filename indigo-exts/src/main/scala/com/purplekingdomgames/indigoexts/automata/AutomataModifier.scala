package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Tint}

sealed trait AutomataModifier
object AutomataModifier {
  case class ChangeAlpha(f: (GameTime, Double) => Double) extends AutomataModifier
  case class ChangeTint(f: (GameTime, Tint) => Tint) extends AutomataModifier
  case class MoveTo(f: (GameTime, Point) => Point) extends AutomataModifier
  case class EmitEvent(f: GameTime => ViewEvent) extends AutomataModifier
}