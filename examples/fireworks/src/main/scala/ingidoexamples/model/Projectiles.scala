package ingidoexamples.model

import indigo.shared.dice.Dice
import indigo.shared.time.Millis
import indigo.shared.collections.NonEmptyList
import indigoexts.geometry.Vertex
import indigo.shared.temporal.Signal
import indigoexts.geometry.Bezier
import indigoexts.subsystems.automata.AutomatonPayload

trait Projectile extends AutomatonPayload {
  val flightTime: Millis
  val movementSignal: Signal[Vertex]
}

object Projectiles {

  def createArcSignal(lifeSpan: Millis): NonEmptyList[Vertex] => Signal[Vertex] =
    Bezier
      .fromVerticesNel(_)
      .toSignal(lifeSpan)
      .clampTime(Millis(0), lifeSpan)

  def pickFlightTime(dice: Dice, min: Millis, max: Millis): Millis =
    if (max === min) {
      min
    } else if (max > min) {
      val diff = max.value - min.value
      Millis(min.value + (dice.rollDouble * diff).toLong)
    } else {
      val diff = min.value - max.value
      Millis(max.value + (dice.rollDouble * diff).toLong)
    }

}
