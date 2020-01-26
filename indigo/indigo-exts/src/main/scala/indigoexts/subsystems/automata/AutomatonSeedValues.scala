package indigoexts.subsystems.automata

import indigo.shared.datatypes.Point
import indigo.shared.time.Millis
import indigo.shared.time.Seconds

sealed trait AutomatonSeedValues {

  val spawnedAt: Point
  val createdAt: Millis
  val lifeSpan: Millis
  val timeAliveDelta: Millis
  val randomSeed: Int
  val payload: Option[AutomatonPayload]

  /**
    * A value progressing from 0 to 1 as the automaton reaches its end.
    */
  def progression: Double =
    timeAliveDelta.value.toDouble / lifeSpan.value.toDouble

  def updateDelta(frameDelta: Seconds): AutomatonSeedValues =
    AutomatonSeedValues(
      spawnedAt,
      createdAt,
      lifeSpan,
      timeAliveDelta + frameDelta.toMillis,
      randomSeed,
      payload
    )

}

object AutomatonSeedValues {

  def apply(spawnPosition: Point, creationTime: Millis, lifeExpectancy: Millis, age: Millis, randomSeedValue: Int, initialPayload: Option[AutomatonPayload]): AutomatonSeedValues =
    new AutomatonSeedValues {
      val spawnedAt: Point             = spawnPosition
      val createdAt: Millis            = creationTime
      val lifeSpan: Millis             = lifeExpectancy
      val timeAliveDelta: Millis       = age
      val randomSeed: Int              = randomSeedValue
      val payload: Option[AutomatonPayload] = initialPayload
    }

}
