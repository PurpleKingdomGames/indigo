package indigoexts.grid

import indigo.gameengine.scenegraph.datatypes.Point
import indigo.runtime.Show

import scala.annotation.tailrec
import scala.util.Random

import indigo.Eq._

final case class GridPoint(x: Int, y: Int) {

  def ===(other: GridPoint): Boolean =
    GridPoint.equality(this, other)

  def +(other: GridPoint): GridPoint =
    GridPoint.append(this, other)

  def <=(other: GridPoint): Boolean =
    GridPoint.lessThanOrEqual(this, other)

  def toPoint: Point =
    Point(x, y)

  def wrap(gridSize: GridSize): GridPoint =
    GridPoint.wrap(this, gridSize)

}
object GridPoint {

  implicit val show: Show[GridPoint] =
    Show.create(p => s"""(${p.x}, ${p.y})""")

  def tupleToGridPoint(t: (Int, Int)): GridPoint =
    GridPoint(t._1, t._2)

  val Up: GridPoint    = GridPoint(0, 1)
  val Down: GridPoint  = GridPoint(0, -1)
  val Left: GridPoint  = GridPoint(-1, 0)
  val Right: GridPoint = GridPoint(1, 0)

  def apply: GridPoint =
    identity

  def identity: GridPoint =
    GridPoint(0, 0)

  def fromPoint(point: Point): GridPoint =
    GridPoint(point.x, point.y)

  def equality(a: GridPoint, b: GridPoint): Boolean =
    a.x === b.x && a.y === b.y

  def append(a: GridPoint, b: GridPoint): GridPoint =
    GridPoint(a.x + b.x, a.y + b.y)

  def lessThanOrEqual(a: GridPoint, b: GridPoint): Boolean =
    a.x <= b.x && a.y <= b.y

  def fillIncrementally(start: GridPoint, end: GridPoint): List[GridPoint] = {
    @tailrec
    def rec(last: GridPoint, dest: GridPoint, p: GridPoint => Boolean, acc: List[GridPoint]): List[GridPoint] =
      if (p(last)) acc
      else {
        val nextX: Int      = if (last.x + 1 <= end.x) last.x + 1 else last.x
        val nextY: Int      = if (last.y + 1 <= end.y) last.y + 1 else last.y
        val next: GridPoint = GridPoint(nextX, nextY)
        rec(next, dest, p, acc :+ next)
      }

    if (start <= end) rec(start, end, (gp: GridPoint) => gp === end, List(start))
    else rec(end, start, (gp: GridPoint) => gp === start, List(end))
  }

  def random(maxX: Int, maxY: Int): GridPoint =
    GridPoint(
      x = Random.nextInt(maxX),
      y = Random.nextInt(maxY)
    )

  def wrap(gridPoint: GridPoint, gridSize: GridSize): GridPoint =
    gridPoint.copy(
      x = if (gridPoint.x < 0) gridSize.columns else gridPoint.x % gridSize.columns,
      y = if (gridPoint.y < 0) gridSize.rows else gridPoint.y    % gridSize.rows
    )

  def linearInterpolation(a: GridPoint, b: GridPoint, divisor: Double, multiplier: Double): Point =
    Point.linearInterpolation(a.toPoint, b.toPoint, divisor, multiplier)

}
