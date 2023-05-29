package indigoexamples.model

import indigo.*
import indigoexamples.automata.TrailAutomata
import indigoextras.subsystems.AutomataEvent
import indigoextras.subsystems.AutomatonPayload

trait Projectile extends AutomatonPayload {
  val flightTime: Seconds
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

  def createArcSignal(lifeSpan: Seconds): NonEmptyBatch[Vertex] => Signal[Vertex] =
    Bezier
      .fromVerticesNonEmpty(_)
      .toSignal(lifeSpan)

  def pickFlightTime(dice: Dice, min: Seconds, max: Seconds): Seconds =
    if (max == min) {
      min
    } else if (max > min) {
      min + ((max - min) * dice.rollDouble).toDouble
    } else {
      max + ((min - max) * dice.rollDouble).toDouble
    }

  def emitTrailEvents(position: Point, tint: RGBA, interval: Seconds): Signal[Batch[AutomataEvent.Spawn]] =
    Signal.Pulse(interval).map { predicate =>
      if (predicate) Batch(TrailAutomata.spawnEvent(position, tint)) else Batch.empty
    }

}
