package ingidoexamples.model

import indigo._
import indigoexts.geometry.Vertex
import indigoexts.geometry.Bezier
import indigoexts.subsystems.automata.AutomatonPayload
import ingidoexamples.automata.TrailAutomata
import indigoexts.subsystems.automata.AutomataEvent

trait Projectile extends AutomatonPayload {
  val flightTime: Millis
  val movementSignal: Signal[Vertex]
}

object Projectiles {

  def toScreenSpace(launchPosition: Point, screenDimensions: Rectangle): SignalFunction[Vertex, Point] =
    SignalFunction { vertex =>
      // This is a positive value, but "Up" is a subtraction...
      val maxAltitude: Int        = ((screenDimensions.height - 5) / 6) * 5
      val maxHorizonalTravel: Int = screenDimensions.width / 2

      Point(
        x = launchPosition.x + (maxHorizonalTravel * vertex.x).toInt,
        y = launchPosition.y - (maxAltitude * vertex.y).toInt
      )
    }

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

  def emitTrailEvents(position: Point): Signal[List[AutomataEvent.Spawn]] =
    Signal.Pulse(Millis(2)).map { predicate =>
      if (predicate) List(TrailAutomata.spawnEvent(position)) else Nil
    }

}
