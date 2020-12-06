package snake.model.quadtrees

import indigo.shared.datatypes.{Point, Rectangle}
import indigoextras.geometry.LineSegment

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigoextras.geometry.Polygon
import indigoextras.geometry.Vertex

import snake.model.grid.GridPoint

trait QuadBounds {
  val x: Int
  val y: Int
  val width: Int
  val height: Int

  def left: Int   = x
  def top: Int    = y
  def right: Int  = x + width
  def bottom: Int = y + height

  def position: GridPoint =
    GridPoint(x, y)

  def center: GridPoint =
    GridPoint(x + (width / 2), y + (height / 2))

  def centerAsVertex: Vertex =
    Vertex(x.toDouble + (width.toDouble / 2d), y.toDouble + (height.toDouble / 2d))

  // Left, Bottom, Right, Top, following points counter clockwise.
  def edges: List[LineSegment] =
    List(
      LineSegment((left.toDouble, top.toDouble), (left.toDouble, bottom.toDouble)),
      LineSegment((left.toDouble, bottom.toDouble), (right.toDouble, bottom.toDouble)),
      LineSegment((right.toDouble, bottom.toDouble), (right.toDouble, top.toDouble)),
      LineSegment((right.toDouble, top.toDouble), (left.toDouble, top.toDouble))
    )

  def isOneUnitSquare: Boolean =
    width === 1 && height === 1

  def subdivide: (QuadBounds, QuadBounds, QuadBounds, QuadBounds) =
    QuadBounds.subdivide(this)

  def isPointWithinBounds(gridPoint: GridPoint): Boolean =
    QuadBounds.pointWithinBounds(this, gridPoint)

  def toRectangle: Rectangle =
    Rectangle(x, y, width, height)

  def toPolygon: Polygon =
    Polygon.fromRectangle(toRectangle)

  def collidesWithRay(lineSegment: LineSegment): Boolean =
    QuadBounds.rayCollisionCheck(this, lineSegment)

  def collidesWithRayAt(lineSegment: LineSegment): Option[Point] =
    QuadBounds.rayCollisionPosition(this, lineSegment)

  override def toString: String =
    s"""QuadBounds(${x.toString()}, ${y.toString}, ${width.toString()}, ${height.toString()})"""

  def ===(other: QuadBounds): Boolean =
    implicitly[EqualTo[QuadBounds]].equal(this, other)

}

object QuadBounds {

  implicit val equalTo: EqualTo[QuadBounds] = {
    val eqI = implicitly[EqualTo[Int]]
    EqualTo.create { (a, b) =>
      eqI.equal(a.x, b.x) && eqI.equal(a.y, b.y) && eqI.equal(a.width, b.width) && eqI.equal(a.height, b.height)
    }
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
      unsafeCreate(
        quadBounds.x + (quadBounds.width / 2),
        quadBounds.y,
        quadBounds.width - (quadBounds.width / 2),
        quadBounds.height / 2
      ),
      unsafeCreate(
        quadBounds.x,
        quadBounds.y + (quadBounds.height / 2),
        quadBounds.width / 2,
        quadBounds.height - (quadBounds.height / 2)
      ),
      unsafeCreate(
        quadBounds.x + (quadBounds.width / 2),
        quadBounds.y + (quadBounds.height / 2),
        quadBounds.width - (quadBounds.width / 2),
        quadBounds.height - (quadBounds.height / 2)
      )
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

  def rayCollisionCheck(bounds: QuadBounds, line: LineSegment): Boolean =
    bounds.edges.exists { edge =>
      line.intersectsWithLine(edge)
    }

  def rayCollisionPosition(bounds: QuadBounds, line: LineSegment): Option[Point] =
    bounds.edges
      .map { edge =>
        line.intersectsAt(edge)
      }
      .collect { case Some(s) => s }
      .sortWith((p1, p2) => line.start.distanceTo(p1) < line.start.distanceTo(p2))
      .headOption
      .map(_.toPoint)
}
