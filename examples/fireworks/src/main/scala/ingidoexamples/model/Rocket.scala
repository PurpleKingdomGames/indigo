package ingidoexamples.model

import indigo._
import indigoexts.geometry.Vertex

final case class Rocket(flightTime: Millis, movementSignal: Signal[Vertex]) extends Projectile

object Rocket {

  def generateRocket(dice: Dice): Rocket = {
    val flightTime = Projectiles.pickFlightTime(dice, Millis(1000L), Millis(2000L))

    val signalFunction: Dice => Signal[Vertex] =
      pickEndPoint andThen
        createArcControlVertices(dice) andThen
        Projectiles.createArcSignal(flightTime)

    Rocket(flightTime, signalFunction(dice))
  }

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

  def pickEndPoint: Dice => Vertex =
    dice => Vertex(-0.5d + (dice.rollDouble * 1), (dice.rollDouble * 0.5d) + 0.5d)

}
