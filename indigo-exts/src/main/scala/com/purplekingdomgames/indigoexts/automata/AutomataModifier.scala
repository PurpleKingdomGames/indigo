package com.purplekingdomgames.indigoexts.automata

import com.purplekingdomgames.indigo.gameengine.GameTime
import com.purplekingdomgames.indigo.gameengine.events.ViewEvent
import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Tint}

sealed trait AutomataModifier
object AutomataModifier {
  case class ChangeAlpha(f: (GameTime, Double) => Double) extends AutomataModifier
  case class ChangeTint(f: (GameTime, Tint) => Tint) extends AutomataModifier
  case class MoveTo(f: (GameTime, Point) => Point) extends AutomataModifier
  case class EmitEvents(f: GameTime => List[ViewEvent]) extends AutomataModifier

  //TODO: Bring this back when you have a real use case.
//  case class Collision(f: ((Rectangle, Rectangle) => Boolean) => List[ViewEvent]) extends AutomataModifier
}