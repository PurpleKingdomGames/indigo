package indigoextras.geometry

import indigo.shared.EqualTo
import indigo.shared.datatypes.Vector2

final case class LineSegment(start: Vertex, end: Vertex) {
  val center: Vertex =
    Vertex(
      ((end.x - start.x) / 2) + start.x,
      ((end.y - start.y) / 2) + start.y
    )

  def left: Double   = Math.min(start.x, end.x)
  def right: Double  = Math.max(start.x, end.x)
  def top: Double    = Math.min(start.y, end.y)
  def bottom: Double = Math.max(start.y, end.y)

  def length: Double =
    start.distanceTo(end)

  def sdf(vertex: Vertex): Double =
    LineSegment.signedDistanceFunction(this, vertex)
  def distanceToBoundary(vertex: Vertex): Double =
    sdf(vertex)

  def moveTo(newPosition: Vertex): LineSegment =
    this.copy(start = newPosition, end = newPosition + (end - start))
  def moveTo(x: Double, y: Double): LineSegment =
    moveTo(Vertex(x, y))

  def moveBy(amount: Vertex): LineSegment =
    moveTo(start + amount)
  def moveBy(x: Double, y: Double): LineSegment =
    moveBy(Vertex(x, y))

  def moveStartTo(newPosition: Vertex): LineSegment =
    this.copy(start = newPosition)
  def moveStartTo(x: Double, y: Double): LineSegment =
    moveStartTo(Vertex(x, y))
  def moveStartBy(amount: Vertex): LineSegment =
    moveStartTo(start + amount)
  def moveStartBy(x: Double, y: Double): LineSegment =
    moveStartBy(Vertex(x, y))

  def moveEndTo(newPosition: Vertex): LineSegment =
    this.copy(end = newPosition)
  def moveEndTo(x: Double, y: Double): LineSegment =
    moveEndTo(Vertex(x, y))
  def moveEndBy(amount: Vertex): LineSegment =
    moveEndTo(end + amount)
  def moveEndBy(x: Double, y: Double): LineSegment =
    moveEndBy(Vertex(x, y))

  def invert: LineSegment =
    LineSegment(end, start)
  def flip: LineSegment =
    invert

  def normal: Vector2 =
    Vector2(
      x = -(end.y - start.y),
      y = (end.x - start.x)
    ).normalise

  def toLine: Line =
    Line.fromLineSegment(this)

  def intersectsAt(other: LineSegment): Option[Vertex] = {
    val res = Line.intersection(this.toLine, other.toLine) match {
      case r @ LineIntersectionResult.NoIntersection =>
        r

      case r @ LineIntersectionResult.IntersectionVertex(_, _) =>
        val pt = r.toVertex
        if (contains(pt) && other.contains(pt)) r
        else LineIntersectionResult.NoIntersection
    }

    res.toOption
  }

  def intersectsWithLine(other: LineSegment): Boolean =
    Line.intersection(this.toLine, other.toLine) match {
      case LineIntersectionResult.NoIntersection =>
        false

      case r @ LineIntersectionResult.IntersectionVertex(_, _) =>
        val pt = r.toVertex
        contains(pt) && other.contains(pt)
    }

  def contains(vertex: Vertex): Boolean =
    LineSegment.lineSegmentContainsVertex(this, vertex, 0.5f)

  def isFacingVertex(vertex: Vertex): Boolean =
    (normal dot Vertex.twoVerticesToVector2(vertex, center)) < 0

  def closestPointOnLine(to: Vertex): Option[Vertex] = {
    val a   = end.y - start.y
    val b   = start.x - end.x
    val c1  = a * start.x + b * start.y
    val c2  = -b * to.x + a * to.y
    val det = a * a - -b * b

    if (det != 0.0)
      Some(
        Vertex(
          x = (a * c1 - b * c2) / det,
          y = (a * c2 - -b * c1) / det
        ).clamp(start, end)
      )
    else
      None
  }

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

  def apply(start: Vertex, end: Vertex): LineSegment =
    new LineSegment(start, end)

  def apply(x1: Double, y1: Double, x2: Double, y2: Double): LineSegment =
    LineSegment(Vertex(x1, y1), Vertex(x2, y2))

  def apply(start: (Double, Double), end: (Double, Double)): LineSegment =
    LineSegment(Vertex.tuple2ToVertex(start), Vertex.tuple2ToVertex(end))

  def lineSegmentContainsVertex(lineSegment: LineSegment, vertex: Vertex, tolerance: Double): Boolean =
    lineSegment.toLine match {
      case Line.InvalidLine =>
        false

      case Line.ParallelToAxisY(_) =>
        if (vertex.x >= lineSegment.start.x - tolerance && vertex.x <= lineSegment.start.x + tolerance && vertex.y >= lineSegment.top && vertex.y <= lineSegment.bottom) true
        else false

      case l @ Line.Components(_, _) =>
        if (vertex.x >= lineSegment.left && vertex.x <= lineSegment.right && vertex.y >= lineSegment.top && vertex.y <= lineSegment.bottom)
          l.slopeComparison(vertex, tolerance)
        else false
    }

  def signedDistanceFunction(lineSegment: LineSegment, vertex: Vertex): Double = {
    val pa: Vertex = vertex - lineSegment.start
    val ba: Vertex = lineSegment.end - lineSegment.start
    val h: Double  = Math.min(1.0, Math.max(0.0, (pa.dot(ba) / ba.dot(ba))))
    (pa - (ba * h)).length
  }

}
