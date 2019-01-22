package indigoexts.quadtrees

import indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}
import indigo.runtime.Show
import indigoexts.grid.GridPoint
import indigoexts.line.{IntersectionResult, LineSegment}

import indigo.Eq._

trait QuadBounds {
  val x: Int
  val y: Int
  val width: Int
  val height: Int

  def left: Int   = x
  def top: Int    = y
  def right: Int  = x + width
  def bottom: Int = y + height

  def position: GridPoint = GridPoint(x, y)
  def center: GridPoint   = GridPoint(x + (width / 2), y + (height / 2))

  // Left, Bottom, Right, Top, following points counter clockwise.
  def edges: List[LineSegment] =
    List(
      LineSegment((left, top), (left, bottom)),
      LineSegment((left, bottom), (right, bottom)),
      LineSegment((right, bottom), (right, top)),
      LineSegment((right, top), (left, top))
    )

  def isOneUnitSquare: Boolean =
    width === 1 && height === 1

  def subdivide: (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    QuadBounds.subdivide(this)

  def isPointWithinBounds(gridPoint: GridPoint): Boolean =
    QuadBounds.pointWithinBounds(this, gridPoint)

  def toRectangle: Rectangle =
    Rectangle(x, y, width, height)

  def collidesWithRay(lineSegment: LineSegment): Boolean =
    QuadBounds.rayCollisionCheck(this, lineSegment)

  def collidesWithRayAt(lineSegment: LineSegment): Option[Point] =
    QuadBounds.rayCollisionPosition(this, lineSegment)

  def renderAsString: String =
    s"""($x, $y, $width, $height)"""

  def ===(other: QuadBounds): Boolean =
    QuadBounds.equals(this, other)

}

object QuadBounds {

  implicit def showQuadBounds[T]: Show[QuadBounds] =
    Show.create(b => b.renderAsString)

  implicit val show: Show[QuadBounds] =
    Show.create { qb =>
      s"""QuadBounds(${qb.x}, ${qb.y}, ${qb.width}, ${qb.height})"""
    }

  def apply(size: Int): QuadBounds =
    unsafeCreate(
      0,
      0,
      if (size < 2) 2 else size,
      if (size < 2) 2 else size
    )

  def apply(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    unsafeCreate(
      if (_x < 0) 0 else _x,
      if (_y < 0) 0 else _y,
      if (_width < 2) 2 else _width,
      if (_height < 2) 2 else _height
    )

  def fromRectangle(rectangle: Rectangle): QuadBounds =
    unsafeCreate(rectangle.x, rectangle.y, rectangle.width, rectangle.height)

  def unsafeCreate(_x: Int, _y: Int, _width: Int, _height: Int): QuadBounds =
    new QuadBounds {
      val x: Int      = _x
      val y: Int      = _y
      val width: Int  = if (_width < 1) 1 else _width
      val height: Int = if (_height < 1) 1 else _height
    }

  def pointWithinBounds(quadBounds: QuadBounds, gridPoint: GridPoint): Boolean =
    gridPoint.x >= quadBounds.left &&
      gridPoint.y >= quadBounds.top &&
      gridPoint.x < quadBounds.right &&
      gridPoint.y < quadBounds.bottom

  def subdivide(quadBounds: QuadBounds): (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    (
      unsafeCreate(quadBounds.x, quadBounds.y, quadBounds.width / 2, quadBounds.height / 2),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2), quadBounds.y, quadBounds.width - (quadBounds.width / 2), quadBounds.height / 2),
      unsafeCreate(quadBounds.x, quadBounds.y + (quadBounds.height / 2), quadBounds.width / 2, quadBounds.height - (quadBounds.height / 2)),
      unsafeCreate(quadBounds.x + (quadBounds.width / 2), quadBounds.y + (quadBounds.height / 2), quadBounds.width - (quadBounds.width / 2), quadBounds.height - (quadBounds.height / 2))
    )

  def combine(head: QuadBounds, tail: List[QuadBounds]): QuadBounds =
    tail.foldLeft(head)((a, b) => append(a, b))

  def append(a: QuadBounds, b: QuadBounds): QuadBounds = {
    val l = Math.min(a.left, b.left)
    val t = Math.min(a.top, b.top)
    val w = Math.max(a.right, b.right) - l
    val h = Math.max(a.bottom, b.bottom) - t

    unsafeCreate(l, t, w, h)
  }

  def equals(a: QuadBounds, b: QuadBounds): Boolean =
    a.x === b.x && a.y === b.y && a.width === b.width && a.height === b.height

  def rayCollisionCheck(bounds: QuadBounds, line: LineSegment): Boolean =
    bounds.edges.exists { edge =>
      line.intersectWith(edge) match {
        case ip: IntersectionResult.IntersectionPoint =>
          line.containsPoint(ip.toPoint)

        case IntersectionResult.NoIntersection =>
          false
      }
    }

  def rayCollisionPosition(bounds: QuadBounds, line: LineSegment): Option[Point] =
    bounds.edges
      .map { edge =>
        line.intersectWith(edge) match {
          case ip @ IntersectionResult.IntersectionPoint(_, _) if line.containsPoint(ip.toPoint) =>
            Some(ip.toPoint)

          case IntersectionResult.IntersectionPoint(_, _) =>
            None

          case IntersectionResult.NoIntersection =>
            None
        }
      }
      .collect { case Some(s) => s }
      .sortWith((p1, p2) => line.start.distanceTo(p1) < line.start.distanceTo(p2))
      .headOption
}
