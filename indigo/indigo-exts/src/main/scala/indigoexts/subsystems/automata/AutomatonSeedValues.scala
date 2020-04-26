package indigoexts.subsystems.automata

import indigo.shared.datatypes.Point
import indigo.shared.time.Seconds

sealed trait AutomatonSeedValues {

  val spawnedAt: Point
  val createdAt: Seconds
  val lifeSpan: Seconds
  val timeAliveDelta: Seconds
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
      timeAliveDelta + frameDelta,
      randomSeed,
      payload
    )

}

object AutomatonSeedValues {

  def apply(spawnPosition: Point, creationTime: Seconds, lifeExpectancy: Seconds, age: Seconds, randomSeedValue: Int, initialPayload: Option[AutomatonPayload]): AutomatonSeedValues =
    new AutomatonSeedValues {
      val spawnedAt: Point             = spawnPosition
      val createdAt: Seconds            = creationTime
      val lifeSpan: Seconds             = lifeExpectancy
      val timeAliveDelta: Seconds       = age
      val randomSeed: Int              = randomSeedValue
      val payload: Option[AutomatonPayload] = initialPayload
    }

}
