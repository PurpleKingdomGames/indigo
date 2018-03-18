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

  /**
    * Collisions are a bit more complex. The idea is that you have a predicate that
    * determines if a collision happened, and if so, it results in a list of ViewEvents
    * being emitted. Why a list? Well you might want multiple outcomes, a bullet for example:
    * Add 10 points to the players score, remove myself by my binding key, and inflict
    * damage on the thing I hit.
    * @param f A predicate that results in a `List[ViewEvent]`
    * @tparam A E.g. Could be a Point or a Rabbit
    * @tparam B E.g. Could be a Rectangle or a Carrot
    */
  case class Collision[A, B](f: ((A, B) => Boolean) => List[ViewEvent]) extends AutomataModifier
}