package ingidoexamples.model

import indigo._
import indigoexts.geometry.Bezier
import indigoexts.subsystems.automata.AutomatonPayload
import indigoexts.geometry.Vertex

final case class Rocket(flightTime: Millis, movementSignal: Signal[Vertex]) extends AutomatonPayload

object Rocket {

  def generateRocket(dice: Dice): Rocket = {
    val flightTime = Rocket.pickFlightTime(dice)

    Rocket(
      flightTime,
      Rocket.createRocketArcSignal(
        dice,
        Rocket.pickEndPoint(dice),
        flightTime
      )
    )
  }

  def createRocketArcSignal(dice: Dice, target: Vertex, lifeSpan: Millis): Signal[Vertex] =
    createRocketArcBezier(dice, target).toSignal(lifeSpan).clampTime(Millis(0), lifeSpan)

  def createRocketArcBezier(dice: Dice, target: Vertex): Bezier =
    Bezier.fromVerticesNel(createArcControlVertices(dice, target))

  def createArcControlVertices(dice: Dice, target: Vertex): NonEmptyList[Vertex] = {

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

  def pickEndPoint(dice: Dice): Vertex =
    Vertex(-0.5d + (dice.rollDouble * 1), (dice.rollDouble * 0.5d) + 0.5d)

  def pickFlightTime(dice: Dice): Millis =
    Millis(((dice.roll(10) + 10) * 100).toLong) // between 1 and 3 seconds...

}
