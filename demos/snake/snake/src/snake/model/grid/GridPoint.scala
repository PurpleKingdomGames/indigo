// package snake.model.grid

// import indigo.shared.datatypes.Point
// import indigo.shared.dice.Dice

// import scala.annotation.tailrec

// final case class GridPoint(x: Int, y: Int) {

//   def +(other: GridPoint): GridPoint =
//     GridPoint.append(this, other)

//   def <=(other: GridPoint): Boolean =
//     GridPoint.lessThanOrEqual(this, other)

//   def toPoint: Point =
//     Point(x, y)

//   def wrap(gridSize: GridSize): GridPoint =
//     GridPoint.wrap(this, gridSize)

// }
// object GridPoint {

//   def tupleToGridPoint(t: (Int, Int)): GridPoint =
//     GridPoint(t._1, t._2)

//   val Up: GridPoint    = GridPoint(0, 1)
//   val Down: GridPoint  = GridPoint(0, -1)
//   val Left: GridPoint  = GridPoint(-1, 0)
//   val Right: GridPoint = GridPoint(1, 0)

//   def apply: GridPoint =
//     identity

//   def identity: GridPoint =
//     GridPoint(0, 0)

//   def fromPoint(point: Point): GridPoint =
//     GridPoint(point.x, point.y)

//   def equality(a: GridPoint, b: GridPoint): Boolean =
//     a.x == b.x && a.y == b.y

//   def append(a: GridPoint, b: GridPoint): GridPoint =
//     GridPoint(a.x + b.x, a.y + b.y)

//   def lessThanOrEqual(a: GridPoint, b: GridPoint): Boolean =
//     a.x <= b.x && a.y <= b.y

//   def fillIncrementally(start: GridPoint, end: GridPoint): List[GridPoint] = {
//     @tailrec
//     def rec(last: GridPoint, dest: GridPoint, p: GridPoint => Boolean, acc: List[GridPoint]): List[GridPoint] =
//       if (p(last)) acc
//       else {
//         val nextX: Int      = if (last.x + 1 <= end.x) last.x + 1 else last.x
//         val nextY: Int      = if (last.y + 1 <= end.y) last.y + 1 else last.y
//         val next: GridPoint = GridPoint(nextX, nextY)
//         rec(next, dest, p, acc :+ next)
//       }

//     if (start <= end) rec(start, end, (gp: GridPoint) => gp == end, List(start))
//     else rec(end, start, (gp: GridPoint) => gp == start, List(end))
//   }

//   def random(dice: Dice, maxX: Int, maxY: Int): GridPoint =
//     GridPoint(
//       x = dice.rollFromZero(maxX),
//       y = dice.rollFromZero(maxY)
//     )

//   def wrap(gridPoint: GridPoint, gridSize: GridSize): GridPoint =
//     gridPoint.copy(
//       x = if (gridPoint.x < 0) gridSize.columns else gridPoint.x % gridSize.columns,
//       y = if (gridPoint.y < 0) gridSize.rows else gridPoint.y % gridSize.rows
//     )

//   def linearInterpolation(a: GridPoint, b: GridPoint, divisor: Double, multiplier: Double): Point =
//     Point.linearInterpolation(a.toPoint, b.toPoint, divisor, multiplier)

// }
