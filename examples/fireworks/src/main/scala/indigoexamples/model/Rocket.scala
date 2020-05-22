package indigoexamples.model

import indigo._
import indigoextras.geometry.Vertex

final case class Rocket(flightTime: Seconds, movementSignal: Signal[Vertex], flares: List[Flare], tint: RGBA) extends Projectile

object Rocket {

  val PI2: Double = Math.PI * 2

  def generateRocket(dice: Dice, launchPadStartPosition: Vertex): Rocket = {
    val flightTime = Projectiles.pickFlightTime(dice, Seconds(1), Seconds(2))
    val endPoint   = pickEndPoint(dice, launchPadStartPosition)

    val signalFunction: Vertex => Signal[Vertex] =
      createArcControlVertices(dice, launchPadStartPosition) andThen
        Projectiles.createArcSignal(flightTime)

    val tint: RGBA =
      pickColour(dice)

    Rocket(flightTime, signalFunction(endPoint), generateFlares(dice, endPoint, tint), tint)
  }

  def pickColour(dice: Dice): RGBA =
    dice.roll(5) match {
      case 1 => RGBA.Red
      case 2 => RGBA.Green
      case 3 => RGBA.Yellow
      case 4 => RGBA.Magenta
      case 5 => RGBA.Cyan
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

  def generateFlares(dice: Dice, startPosition: Vertex, tint: RGBA): List[Flare] = {
    val count = dice.roll(3) + 4
    (0 to count).toList.map(c => (PI2 / count) * c).map { angle =>
      Flare.generateFlare(dice, startPosition, Radians(angle), tint)
    }
  }

}
