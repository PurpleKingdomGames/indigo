package indigoexamples.model

import indigo._
import indigoexts.geometry.Vertex
import indigoexts.geometry.Bezier
import indigoexts.subsystems.automata.AutomatonPayload
import indigoexamples.automata.TrailAutomata
import indigoexts.subsystems.automata.AutomataEvent

trait Projectile extends AutomatonPayload {
  val flightTime: Millis
  val movementSignal: Signal[Vertex]
}

object Projectiles {

  def toScreenSpace(screenDimensions: Rectangle): Vertex => Point = { vertex =>
    val maxWidth: Int         = screenDimensions.width / 2
    val maxHeight: Int        = (screenDimensions.height / 6) * 5
    val horizontalCenter: Int = ((screenDimensions.width - maxWidth) / 2) + (maxWidth / 2)
    val verticalBottom: Int   = ((screenDimensions.height - maxHeight) / 2) + maxHeight

    Point(
      x = (((maxWidth.toDouble / 2) * vertex.x).toInt) + horizontalCenter,
      y = (-(maxHeight.toDouble * vertex.y).toInt) + verticalBottom
    )
  }

  def createArcSignal(lifeSpan: Millis): NonEmptyList[Vertex] => Signal[Vertex] =
    Bezier
      .fromVerticesNel(_)
      .toSignal(lifeSpan)

  def pickFlightTime(dice: Dice, min: Millis, max: Millis): Millis =
    if (max === min) {
      min
    } else if (max > min) {
      Millis(min.value + (dice.rollDouble * (max.value - min.value)).toLong)
    } else {
      Millis(max.value + (dice.rollDouble * (min.value - max.value)).toLong)
    }

  def emitTrailEvents(position: Point, tint: Tint, interval: Long): Signal[List[AutomataEvent.Spawn]] =
    Signal.Pulse(Millis(interval)).map { predicate =>
      if (predicate) List(TrailAutomata.spawnEvent(position, tint)) else Nil
    }

}
