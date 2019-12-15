package indigoexamples.model

import indigo._
import indigoexts.geometry.Vertex

final case class Rocket(flightTime: Millis, movementSignal: Signal[Vertex], flares: List[Flare], tint: Tint) extends Projectile

object Rocket {

  val PI2: Double = Math.PI * 2

  def generateRocket(dice: Dice, launchPadStartPosition: Vertex): Rocket = {
    val flightTime = Projectiles.pickFlightTime(dice, Millis(1000L), Millis(2000L))
    val endPoint   = pickEndPoint(dice, launchPadStartPosition)

    val signalFunction: Vertex => Signal[Vertex] =
      createArcControlVertices(dice, launchPadStartPosition) andThen
        Projectiles.createArcSignal(flightTime)

    val tint: Tint =
      pickColour(dice)

    Rocket(flightTime, signalFunction(endPoint).easeOut(flightTime, 25), generateFlares(dice, endPoint, tint), tint)
  }

  def pickColour(dice: Dice): Tint =
    dice.roll(5) match {
      case 1 => Tint.Red
      case 2 => Tint.Green
      case 3 => Tint.Blue
      case 4 => Tint.Magenta
      case 5 => Tint.Cyan
    }

  def createArcControlVertices(dice: Dice, launchPadStartPosition: Vertex): Vertex => NonEmptyList[Vertex] =
    target => {
      val baseValue: Double =
        (0.5d * Math.max(0, Math.min(1.0d, dice.rollDouble))) + 0.5d

      NonEmptyList(
        launchPadStartPosition,
        Vertex(
          x = if (target.x < 0) -(baseValue * target.x) else baseValue * target.x,
          y = target.y
        ),
        target
      )
    }

  def pickEndPoint(dice: Dice, launchPadStartPosition: Vertex): Vertex =
    launchPadStartPosition + Vertex(dice.rollDouble - 0.5d, (dice.rollDouble * 0.5d) + 0.5d)

  def generateFlares(dice: Dice, startPosition: Vertex, tint: Tint): List[Flare] = {
    val count = dice.roll(3) + 4
    (0 to count).toList.map(c => (PI2 / count) * c).map { angle =>
      Flare.generateFlare(dice, startPosition, Radians(angle), tint)
    }
  }

}
