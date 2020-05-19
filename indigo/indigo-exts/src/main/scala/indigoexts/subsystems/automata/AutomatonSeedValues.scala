package indigoexts.subsystems.automata

import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

final class AutomatonSeedValues(
    val spawnedAt: Point,
    val createdAt: Seconds,
    val lifeSpan: Seconds,
    val timeAliveDelta: Seconds,
    val randomSeed: Int,
    val payload: Option[AutomatonPayload]
) {

  /**
    * A value progressing from 0 to 1 as the automaton reaches its end.
    */
  def progression: Double =
    timeAliveDelta.toDouble / lifeSpan.toDouble

  def updateDelta(frameDelta: Seconds): AutomatonSeedValues =
    new AutomatonSeedValues(
      spawnedAt,
      createdAt,
      lifeSpan,
      timeAliveDelta + frameDelta,
      randomSeed,
      payload
    )

}
