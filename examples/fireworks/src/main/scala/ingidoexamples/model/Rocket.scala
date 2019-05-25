package ingidoexamples.model

import indigo._
import indigoexts.geometry.Bezier

object Rocket {

  def createRocketArcSignal(dice: Dice, start: Point, end: Point, lifeSpan: Millis): Signal[Point] =
    createRocketArcBezier(dice, start, end).toSignal(lifeSpan).clampTime(Millis(0), lifeSpan)

  def createRocketArcBezier(dice: Dice, start: Point, end: Point): Bezier =
    Bezier.fromPointsNel(createArcControlPoints(dice, start, end))

  def createArcControlPoints(dice: Dice, start: Point, end: Point): NonEmptyList[Point] = {
    val x = Math.max(Int.MinValue, Math.min(Int.MaxValue, (dice.roll(end.x - start.x) - 1) + (if (start.x <= end.x) start.x else end.x)))
    val y = end.y

    NonEmptyList(start, Point(x, y), end)
  }

}
