package indigo.shared.geometry

import indigo.shared.collections.Batch

/** Defines a line in terms of y = mx + b
  */
sealed trait Line derives CanEqual:
  def intersectsWith(other: Line): Boolean
  def intersectsAt(other: Line): Option[Vertex]

  def ~==(other: Line): Boolean =
    def equalish(a: Double, b: Double): Boolean =
      Math.abs(a - b) < 0.0001

    (this, other) match
      case (Line.Components(m1, b1), Line.Components(m2, b2)) =>
        equalish(m1, m2) && equalish(b1, b2)

      case (Line.ParallelToAxisY(xPosition1), Line.ParallelToAxisY(xPosition2)) =>
        equalish(xPosition1, xPosition2)

      case (Line.InvalidLine, Line.InvalidLine) =>
        true

      case _ =>
        false

object Line:
  final case class Components(m: Double, b: Double) extends Line:

    /** This is a slope comparison function. Any point on the line should have the same slope as the line, however, this
      * fails in the case where the x position of the vertex is 0.
      */
    def slopeComparison(vertex: Vertex, tolerance: Double): Boolean =
      val m2: Double =
        if vertex.x == 0 then 0
        else (b - vertex.y) / (0 - vertex.x)

      val mDelta: Double =
        m - m2

      mDelta >= -tolerance && mDelta <= tolerance

    def intersectsWith(other: Line): Boolean =
      other match
        case Components(m2, _) if m == m2 =>
          false

        case Components(_, _) =>
          true

        case ParallelToAxisY(_) =>
          true

        case InvalidLine =>
          false

    def intersectsAt(other: Line): Option[Vertex] =
      other match
        case Components(m2, _) if m == m2 =>
          None

        case Components(m2, b2) =>
          val x: Double = (b2 - b) / (m - m2)
          Some(Vertex(x, (m * x) + b))

        case ParallelToAxisY(x) =>
          Some(Vertex(x, (m * x) + b))

        case _ =>
          None

  final case class ParallelToAxisY(xPosition: Double) extends Line:

    def intersectsWith(other: Line): Boolean =
      other match
        case _: Components =>
          true

        case _ =>
          false

    def intersectsAt(other: Line): Option[Vertex] =
      other match
        case Components(m, b) =>
          Some(Vertex(xPosition, (m * xPosition) + b))

        case _ =>
          None

  case object InvalidLine extends Line:
    def intersectsWith(other: Line): Boolean =
      false

    def intersectsAt(other: Line): Option[Vertex] =
      None

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  def fromLineSegment(lineSegment: LineSegment): Line =
    (lineSegment.start, lineSegment.end) match
      case (Vertex(x1, y1), Vertex(x2, y2)) if x1 == x2 && y1 == y2 =>
        Line.InvalidLine

      case (Vertex(x1, _), Vertex(x2, _)) if x1 == x2 =>
        Line.ParallelToAxisY(x1)

      case (Vertex(x1, y1), Vertex(x2, y2)) =>
        val m: Double = (y2 - y1) / (x2 - x1)

        Line.Components(m, y1 - (m * x1))

  def intersection(l1: Line, l2: Line): LineIntersectionResult =
    /*
    y-intercept = mx + b (i.e. y = mx + b)
    x-intercept = -b/m   (i.e. x = -b/m where y is moved to 0)
     */
    (l1, l2) match
      case (Line.Components(m1, _), Line.Components(m2, _)) if m1 == m2 =>
        // Same slope, so parallel
        LineIntersectionResult.NoIntersection

      case (Line.Components(m1, b1), Line.Components(m2, b2)) =>
        // x = -b/m
        val x: Double = (b2 - b1) / (m1 - m2)

        // y = mx + b
        val y: Double = (m1 * x) + b1

        LineIntersectionResult.IntersectionVertex(x, y)

      case (Line.ParallelToAxisY(x), Line.Components(m, b)) =>
        LineIntersectionResult.IntersectionVertex(
          x = x,
          y = (m * x) + b
        )

      case (Line.Components(m, b), Line.ParallelToAxisY(x)) =>
        LineIntersectionResult.IntersectionVertex(
          x = x,
          y = (m * x) + b
        )

      case _ =>
        LineIntersectionResult.NoIntersection

sealed trait LineIntersectionResult:
  def toOption: Option[Vertex]
  def toBatch: Batch[Vertex]
  def hasIntersected: Boolean

object LineIntersectionResult:
  final case class IntersectionVertex(x: Double, y: Double) extends LineIntersectionResult:
    def toVertex: Vertex =
      Vertex(x, y)

    def toOption: Option[Vertex] =
      Some(toVertex)

    def toBatch: Batch[Vertex] =
      Batch(toVertex)

    def hasIntersected: Boolean =
      true

  case object NoIntersection extends LineIntersectionResult:
    def toOption: Option[Vertex] = None
    def toBatch: Batch[Vertex]   = Batch.empty
    def hasIntersected: Boolean  = false
