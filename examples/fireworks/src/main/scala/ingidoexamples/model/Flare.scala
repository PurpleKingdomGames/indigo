package ingidoexamples.model

import indigo.shared.temporal.Signal
import indigoexts.geometry.Vertex
import indigo.shared.dice.Dice
import indigo.shared.time.Millis
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Radians

final case class Flare(flightTime: Millis, movementSignal: Signal[Vertex]) extends Projectile

object Flare {

  def generateFlare(dice: Dice, startPosition: Vertex, initialAngle: Radians, radius: Double): Flare = {
    val flightTime = Projectiles.pickFlightTime(dice, Millis(750L), Millis(1250L))

    val signalFunction: Dice => Signal[Vertex] =
      pickEndPoint(initialAngle, radius) andThen
        createArcControlVertices(startPosition) andThen
        Projectiles.createArcSignal(flightTime)

    Flare(flightTime, signalFunction(dice))
  }

  def createArcControlVertices(startPosition: Vertex): Vertex => NonEmptyList[Vertex] =
    target => NonEmptyList(startPosition, startPosition + target)

  def pickEndPoint(initialAngle: Radians, radius: Double): Dice => Vertex =
    dice =>
      Vertex(
        Math.sin(wobble(dice, initialAngle.value - 0.2d, initialAngle.value + 0.2d)),
        Math.cos(wobble(dice, initialAngle.value - 0.2d, initialAngle.value + 0.2d))
      ) * Vertex(radius, radius)

  def wobble(dice: Dice, low: Double, high: Double): Double =
    low + ((high - low) * dice.rollDouble)

}
