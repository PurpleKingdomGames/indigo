package ingidoexamples.model

import indigo._
import indigoexts.geometry.Vertex

final case class Rocket(flightTime: Millis, movementSignal: Signal[Vertex], flares: List[Flare]) extends Projectile

object Rocket {

  val PI2: Double = Math.PI * 2

  def generateRocket(dice: Dice, launchPadStartPosition: Vertex): Rocket = {
    val flightTime = Projectiles.pickFlightTime(dice, Millis(1000L), Millis(2000L))
    val endPoint   = pickEndPoint(dice, launchPadStartPosition)

    val signalFunction: Signal[Vertex] =
      (createArcControlVertices(dice, launchPadStartPosition) andThen
        Projectiles.createArcSignal(flightTime))(endPoint)

    Rocket(flightTime, signalFunction, generateFlares(dice, endPoint.toPoint))
  }

  def createArcControlVertices(dice: Dice, launchPadStartPosition: Vertex): Vertex => NonEmptyList[Vertex] =
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

      NonEmptyList(launchPadStartPosition, Vertex(x, y), target)
    }

  def pickEndPoint(dice: Dice, launchPadStartPosition: Vertex): Vertex =
    launchPadStartPosition + Vertex(dice.rollDouble - 0.5d, (dice.rollDouble * 0.5d) + 0.5d)

  def generateFlares(dice: Dice, startPoint: Point): List[Flare] =
    List.fill(dice.roll(3) + 4)(dice.rollDouble * PI2).map { angle =>
      Flare.generateFlare(dice, startPoint, Radians(angle), (dice.roll(90) + 10).toDouble)
    }

}
