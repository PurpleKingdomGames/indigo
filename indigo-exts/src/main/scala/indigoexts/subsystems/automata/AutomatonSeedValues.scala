package indigoexts.subsystems.automata

import indigo.shared.datatypes.Point
import indigo.shared.time.Millis

final case class AutomatonSeedValues(spawnedAt: Point, createdAt: Millis, lifeSpan: Millis, timeAliveDelta: Millis, randomSeed: Int) {

  /**
   * A value progressing from 0 to 1 as the automaton reaches its end.
   */
  def progression: Double =
    timeAliveDelta.value.toDouble / lifeSpan.value.toDouble

}
