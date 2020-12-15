package indigoextras.geometry

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

  def sdf(vertex: Vertex): Double = {
    val pa: Vertex = vertex - start
    val ba: Vertex = end - start
    val h: Double  = Math.min(1.0, Math.max(0.0, (pa.dot(ba) / ba.dot(ba))))
    (pa - (ba * h)).length
  }
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

  def intersectsAt(other: LineSegment): Option[Vertex] =
    toLine.intersectsAt(other.toLine).flatMap { pt =>
      if (contains(pt) && other.contains(pt)) Some(pt)
      else None
    }

  def intersectsWithLine(other: LineSegment): Boolean =
    toLine
      .intersectsAt(other.toLine)
      .map(pt => contains(pt) && other.contains(pt))
      .getOrElse(false)

  def contains(vertex: Vertex): Boolean =
    if (vertex.x >= left && vertex.x <= right && vertex.y >= top && vertex.y <= bottom)
      toLine match {
        case Line.InvalidLine =>
          false

        case _: Line.ParallelToAxisY =>
          true

        case l: Line.Components =>
          l.slopeComparison(vertex, 0.5f)
      }
    else false

  def isFacingVertex(vertex: Vertex): Boolean =
    (normal.dot(vertex.makeVectorWith(center))) < 0

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

}

object LineSegment {

  def apply(x1: Double, y1: Double, x2: Double, y2: Double): LineSegment =
    LineSegment(Vertex(x1, y1), Vertex(x2, y2))

  def apply(start: (Double, Double), end: (Double, Double)): LineSegment =
    LineSegment(Vertex.tuple2ToVertex(start), Vertex.tuple2ToVertex(end))

}
