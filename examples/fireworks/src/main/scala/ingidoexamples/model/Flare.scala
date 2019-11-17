package ingidoexamples.model

import indigo.shared.temporal.Signal
import indigoexts.geometry.Vertex
import indigo.shared.dice.Dice
import indigo.shared.time.Millis
import indigoexts.geometry.Bezier
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Radians

// TODO: Test all the things.
final case class Flare(flightTime: Millis, movementSignal: Signal[Vertex])

object Flare {

  def generate(dice: Dice, initialAngle: Radians): Flare = {
    val flightTime = pickFlightTime(dice)

    val signalFunction: Dice => Signal[Vertex] =
      pickEndPoint(initialAngle) andThen
        createArcControlVertices(dice) andThen
        createArcSignal(flightTime)

    Flare(flightTime, signalFunction(dice))
  }

  def createArcSignal(lifeSpan: Millis): NonEmptyList[Vertex] => Signal[Vertex] =
    Bezier
      .fromVerticesNel(_)
      .toSignal(lifeSpan)
      .clampTime(Millis(0), lifeSpan)

  def createArcControlVertices(dice: Dice): Vertex => NonEmptyList[Vertex] =
    target => {
      val baseValue: Double =
        (0.5d * Math.max(0, Math.min(1.0d, dice.rollDouble))) + 0.5d

      val x: Double =
        ({ (positiveX: Double) =>
          if (target.x < 0)
            -(positiveX * target.x)
          else
            positiveX * target.x
        })(baseValue)

      val y: Double =
        target.y

      NonEmptyList(Vertex.zero, Vertex(x, y), target)
    }

  // to test
  def pickEndPoint(initialAngle: Radians): Dice => Vertex =
    dice =>
      Vertex(
        Math.sin(wobble(dice, initialAngle.value - 0.2d, initialAngle.value + 0.2d)),
        Math.cos(wobble(dice, initialAngle.value - 0.2d, initialAngle.value + 0.2d))
      )

  // to test
  def wobble(dice: Dice, low: Double, high: Double): Double =
    low + ((high - low) * dice.rollDouble)

  // to test
  def pickFlightTime(dice: Dice): Millis =
    Millis(((dice.roll(10) + 10) * 100).toLong)

}
