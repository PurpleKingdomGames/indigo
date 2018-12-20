package indigoexts.automata

import indigo.gameengine.GameTime
import indigo.gameengine.events.GlobalEvent
import indigo.gameengine.scenegraph.datatypes.{Point, Tint}

sealed trait AutomataModifier extends Product with Serializable
object AutomataModifier {
  final case class ChangeAlpha(f: (GameTime, AutomatonSeedValues, Double) => Double)   extends AutomataModifier
  final case class ChangeTint(f: (GameTime, AutomatonSeedValues, Tint) => Tint)        extends AutomataModifier
  final case class MoveTo(f: (GameTime, AutomatonSeedValues, Point) => Point)          extends AutomataModifier
  final case class EmitEvents(f: (GameTime, AutomatonSeedValues) => List[GlobalEvent]) extends AutomataModifier
}
