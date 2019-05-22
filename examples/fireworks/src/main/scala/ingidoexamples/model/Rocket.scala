package ingidoexamples.model

// import indigoexts.geometry.Bezier
import indigo.shared.datatypes.Point
import indigo.shared.dice.Dice

object Rocket {

  // def createRocketArc(start: Point, end: Point): Bezier =
  //   ???

  def createArcControlPoints(dice: Dice, start: Point, end: Point): List[Point] = {
    val x = Math.max(Int.MinValue, Math.min(Int.MaxValue, (dice.roll(end.x - start.x) - 1) + (if (start.x <= end.x) start.x else end.x)))
    val y = end.y

    List(start, Point(x, y), end)
  }

}
