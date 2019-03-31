package indigoexts.automata

import indigo.Point
import indigo.Millis

final class AutomatonSeedValues(
    val spawnedAt: Point,
    val createdAt: Millis,
    val lifeSpan: Millis,
    val timeAlive: Millis,
    val randomSeed: Int
) {
  def updateTimeAlive(frameDelta: Millis): AutomatonSeedValues =
    new AutomatonSeedValues(
      spawnedAt,
      createdAt,
      lifeSpan,
      timeAlive + frameDelta,
      randomSeed
    )
}
object AutomatonSeedValues {

  def apply(spawnedAt: Point, createdAt: Millis, lifeSpan: Millis, timeAlive: Millis, randomSeed: Int): AutomatonSeedValues =
    new AutomatonSeedValues(spawnedAt, createdAt, lifeSpan, timeAlive, randomSeed)

}
