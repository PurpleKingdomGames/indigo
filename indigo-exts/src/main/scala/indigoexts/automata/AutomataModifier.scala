package indigoexts.automata

import indigo.gameengine.GameTime
import indigo.gameengine.events.ViewEvent
import indigo.gameengine.scenegraph.datatypes.{Point, Tint}

sealed trait AutomataModifier extends Product with Serializable
object AutomataModifier {
  case class ChangeAlpha(f: (GameTime, AutomatonSeedValues, Double) => Double) extends AutomataModifier
  case class ChangeTint(f: (GameTime, AutomatonSeedValues, Tint) => Tint)      extends AutomataModifier
  case class MoveTo(f: (GameTime, AutomatonSeedValues, Point) => Point)        extends AutomataModifier
  case class EmitEvents(f: (GameTime, AutomatonSeedValues) => List[ViewEvent]) extends AutomataModifier
}
