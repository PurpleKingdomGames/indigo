package indigoexts.geometry

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.AsString
import indigo.shared.datatypes.Vector2

final class LineSegment(val start: Vertex, val end: Vertex) {
  val center: Vertex =
    Vertex(
      ((end.x - start.x) / 2) + start.x,
      ((end.y - start.y) / 2) + start.y
    )

  def left: Double   = Math.min(start.x, end.x)
  def right: Double  = Math.max(start.x, end.x)
  def top: Double    = Math.min(start.y, end.y)
  def bottom: Double = Math.max(start.y, end.y)

  def normal: Vector2 =
    LineSegment.calculateNormal(start, end)

  def lineProperties: LineProperties =
    LineSegment.calculateLineComponents(start, end)

  def intersectWith(other: LineSegment): IntersectionResult =
    LineSegment.intersection(this, other)

  def intersectWithLine(other: LineSegment): Boolean =
    LineSegment.intersection(this, other) match {
      case IntersectionResult.NoIntersection =>
        false

      case r @ IntersectionResult.IntersectionVertex(_, _) =>
        val pt = r.toVertex
        containsVertex(pt) && other.containsVertex(pt)

    }

  def containsVertex(vertex: Vertex): Boolean =
    LineSegment.lineContainsVertex(this, vertex, 0.5f)

  def isFacingVertex(vertex: Vertex): Boolean =
    LineSegment.isFacingVertex(this, vertex)

  def asString: String =
    implicitly[AsString[LineSegment]].show(this)

  override def toString: String =
    asString

  def ===(other: LineSegment): Boolean =
    implicitly[EqualTo[LineSegment]].equal(this, other)
}

object LineSegment {

  implicit val lsEqualTo: EqualTo[LineSegment] = {
    val eqPt = implicitly[EqualTo[Vertex]]

    EqualTo.create { (a, b) =>
      eqPt.equal(a.start, b.start) && eqPt.equal(a.end, b.end)
    }
  }

  implicit val lsAsString: AsString[LineSegment] = {
    val s = implicitly[AsString[Vertex]]

    AsString.create { ls =>
      s"LineSegment(start = ${s.show(ls.start)}, end = ${s.show(ls.end)})"
    }
  }

  def apply(start: Vertex, end: Vertex): LineSegment =
    new LineSegment(start, end)

  def apply(x1: Double, y1: Double, x2: Double, y2: Double): LineSegment =
    LineSegment(Vertex(x1, y1), Vertex(x2, y2))

  def apply(start: (Double, Double), end: (Double, Double)): LineSegment =
    LineSegment(Vertex.tuple2ToVertex(start), Vertex.tuple2ToVertex(end))

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  def calculateLineComponents(start: Vertex, end: Vertex): LineProperties =
    (start, end) match {
      case (Vertex(x1, y1), Vertex(x2, y2)) if x1 === x2 && y1 === y2 =>
        LineProperties.InvalidLine

      case (Vertex(x1, _), Vertex(x2, _)) if x1 === x2 =>
        LineProperties.ParallelToAxisY

      case (Vertex(_, y1), Vertex(_, y2)) if y1 === y2 =>
        LineProperties.ParallelToAxisX

      case (Vertex(x1, y1), Vertex(x2, y2)) =>
        val m: Double = (y2 - y1) / (x2 - x1)

        LineProperties.LineComponents(m, y1 - (m * x1))
    }

  def intersection(l1: LineSegment, l2: LineSegment): IntersectionResult =
    /*
    y-intercept = mx + b (i.e. y = mx + b)
    x-intercept = -b/m   (i.e. x = -b/m where y is moved to 0)
     */
    (l1.lineProperties, l2.lineProperties) match {
      case (LineProperties.LineComponents(m1, b1), LineProperties.LineComponents(m2, b2)) =>
        //x = -b/m
        val x: Double = (b2 - b1) / (m1 - m2)

        //y = mx + b
        val y: Double = (m1 * x) + b1

        IntersectionResult.IntersectionVertex(x, y)

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisX) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisY) =>
        IntersectionResult.NoIntersection

      case (LineProperties.ParallelToAxisX, LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionVertex(l2.start.x, l1.start.y)

      case (LineProperties.ParallelToAxisY, LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionVertex(l1.start.x, l2.start.y)

      case (LineProperties.ParallelToAxisX, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionVertex(
          x = (-b / m) - l1.start.y,
          y = l1.start.y
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisX) =>
        IntersectionResult.IntersectionVertex(
          x = (-b / m) - l2.start.y,
          y = l2.start.y
        )

      case (LineProperties.ParallelToAxisY, LineProperties.LineComponents(m, b)) =>
        IntersectionResult.IntersectionVertex(
          x = l1.start.x,
          y = (m * l1.start.x) + b
        )

      case (LineProperties.LineComponents(m, b), LineProperties.ParallelToAxisY) =>
        IntersectionResult.IntersectionVertex(
          x = l2.start.x,
          y = (m * l2.start.x) + b
        )

      case (LineProperties.InvalidLine, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (_, LineProperties.InvalidLine) =>
        IntersectionResult.NoIntersection

      case (LineProperties.InvalidLine, _) =>
        IntersectionResult.NoIntersection

      case _ =>
        IntersectionResult.NoIntersection
    }

  def calculateNormal(start: Vertex, end: Vertex): Vector2 =
    normaliseVertex(Vector2(-(end.y - start.y), (end.x - start.x)))

  def normaliseVertex(vec2: Vector2): Vector2 = {
    val x: Double = vec2.x
    val y: Double = vec2.y

    Vector2(
      if (x === 0) 0 else (x / Math.abs(x)),
      if (y === 0) 0 else (y / Math.abs(y))
    )
  }

  def lineContainsVertex(lineSegment: LineSegment, point: Vertex): Boolean =
    lineContainsVertex(lineSegment, point, 0.01d)

  def lineContainsVertex(lineSegment: LineSegment, point: Vertex, tolerance: Double): Boolean =
    lineSegment.lineProperties match {
      case LineProperties.InvalidLine =>
        false

      case LineProperties.ParallelToAxisX =>
        if (point.y === lineSegment.start.y && point.x >= lineSegment.left && point.x <= lineSegment.right) true
        else false

      case LineProperties.ParallelToAxisY =>
        if (point.x === lineSegment.start.x && point.y >= lineSegment.top && point.y <= lineSegment.bottom) true
        else false

      case LineProperties.LineComponents(m, b) =>
        if (point.x >= lineSegment.left && point.x <= lineSegment.right && point.y >= lineSegment.top && point.y <= lineSegment.bottom) {
          slopeCheck(point.x, point.y, m, b, tolerance)
        } else false
    }

  def lineContainsCoords(lineSegment: LineSegment, coords: (Double, Double), tolerance: Double): Boolean =
    lineContainsXY(lineSegment, coords._1, coords._2, tolerance)

  def lineContainsXY(lineSegment: LineSegment, x: Double, y: Double, tolerance: Double): Boolean =
    lineSegment.lineProperties match {
      case LineProperties.InvalidLine =>
        false

      case LineProperties.ParallelToAxisX =>
        if (y === lineSegment.start.y && x >= lineSegment.left && x <= lineSegment.right) true
        else false

      case LineProperties.ParallelToAxisY =>
        if (x === lineSegment.start.x && y >= lineSegment.top && y <= lineSegment.bottom) true
        else false

      case LineProperties.LineComponents(m, b) =>
        if (x >= lineSegment.left && x <= lineSegment.right && y >= lineSegment.top && y <= lineSegment.bottom)
          slopeCheck(x, y, m, b, tolerance)
        else false
    }

  def slopeCheck(x: Double, y: Double, m: Double, b: Double, tolerance: Double): Boolean = {
    // This is a slope comparison.. Any point on the line should have the same slope as the line.
    val m2: Double =
      if (x === 0) 0
      else (b - y) / (0 - x)

    val mDelta: Double =
      m - m2

    mDelta >= -tolerance && mDelta <= tolerance
  }

  def isFacingVertex(line: LineSegment, vertex: Vertex): Boolean =
    (line.normal dot Vertex.twoVerticesToVector2(vertex, line.center)) < 0

}

sealed trait LineProperties
object LineProperties {
// y = mx + b
  final case class LineComponents(m: Double, b: Double) extends LineProperties
  case object ParallelToAxisX                           extends LineProperties
  case object ParallelToAxisY                           extends LineProperties
  case object InvalidLine                               extends LineProperties
}

sealed trait IntersectionResult
object IntersectionResult {
  final case class IntersectionVertex(x: Double, y: Double) extends IntersectionResult {
    def toVertex: Vertex =
      Vertex(x, y)
  }
  case object NoIntersection extends IntersectionResult
}
