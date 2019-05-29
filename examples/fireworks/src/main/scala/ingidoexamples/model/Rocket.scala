package ingidoexamples.model

import indigo._
import indigoexts.geometry.Bezier
import indigoexts.subsystems.automata.AutomatonPayload

final case class Rocket(startPosition: Point, flightTime: Millis, movementSignal: Signal[Point]) extends AutomatonPayload

object Rocket {

  def generateRocket(dice: Dice, startPosition: Point, screenDimensions: Rectangle): Rocket = {
    val flightTime = Rocket.pickFlightTime(dice)

    Rocket(
      startPosition,
      flightTime,
      Rocket.createRocketArcSignal(
        dice,
        startPosition,
        Rocket.pickEndPoint(dice, startPosition, screenDimensions),
        flightTime
      )
    )
  }

  def createRocketArcSignal(dice: Dice, start: Point, end: Point, lifeSpan: Millis): Signal[Point] =
    createRocketArcBezier(dice, start, end).toSignal(lifeSpan).clampTime(Millis(0), lifeSpan)

  def createRocketArcBezier(dice: Dice, start: Point, end: Point): Bezier =
    Bezier.fromPointsNel(createArcControlPoints(dice, start, end))

  def createArcControlPoints(dice: Dice, start: Point, end: Point): NonEmptyList[Point] = {
    val x = Math.max(Int.MinValue, Math.min(Int.MaxValue, (dice.roll(end.x - start.x) - 1) + (if (start.x <= end.x) start.x else end.x)))
    val y = end.y

    NonEmptyList(start, Point(x, y), end)
  }

  def pickEndPoint(dice: Dice, start: Point, screenDimensions: Rectangle): Point =
    Point(
      x = Math.min(
        screenDimensions.right,
        Math.max(
          screenDimensions.left,
          start.x + dice.roll(start.y - (start.y / 2))
        )
      ),
      y = ({ (div5: Int) =>
        start.y - div5 - dice.roll(div5 * 3)
      })(start.y / 5)
    )

  def pickFlightTime(dice: Dice): Millis =
    Millis(((dice.roll(20) + 10) * 100).toLong) // between 1 and 3 seconds...

}
