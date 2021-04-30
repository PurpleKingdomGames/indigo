package indigoexamples.model

import indigo.shared.temporal.Signal
import indigoextras.geometry.Vertex
import indigo.shared.dice.Dice
import indigo.shared.time.Seconds
import indigo.shared.collections.NonEmptyList
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.RGBA

final case class Flare(flightTime: Seconds, movementSignal: Signal[Vertex], tint: RGBA) extends Projectile

object Flare {

  def generateFlare(dice: Dice, startPosition: Vertex, initialAngle: Radians, tint: RGBA): Flare = {
    val flightTime = Projectiles.pickFlightTime(dice, Seconds(0.3), Seconds(0.5))

    val signalFunction: Dice => Signal[Vertex] =
      pickEndPoint(initialAngle) andThen
        createArcControlVertices(startPosition) andThen
        Projectiles.createArcSignal(flightTime)

    Flare(flightTime, signalFunction(dice), tint)
  }

  def createArcControlVertices(startPosition: Vertex): Vertex => NonEmptyList[Vertex] =
    target => NonEmptyList(startPosition, startPosition + target)

  def pickEndPoint(initialAngle: Radians): Dice => Vertex =
    dice => {
      val radius: Double =
        dice.rollDouble * 0.3d

      Vertex(
        Math.sin(wobble(dice, (initialAngle - 0.2d).toDouble, (initialAngle + 0.2d).toDouble)),
        Math.cos(wobble(dice, (initialAngle - 0.2d).toDouble, (initialAngle + 0.2d).toDouble))
      ) * Vertex(radius * 3.0d, radius)
    }

  def wobble(dice: Dice, low: Double, high: Double): Double =
    low + ((high - low) * dice.rollDouble)

}
