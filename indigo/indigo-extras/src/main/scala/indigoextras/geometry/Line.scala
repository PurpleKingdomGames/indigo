package indigoextras.geometry

import indigo.shared.EqualTo._

/**
 * Defines a line in terms of y = mx + b
 */
sealed trait Line
object Line {
  final case class Components(m: Double, b: Double) extends Line {

    def slopeComparison(vertex: Vertex, tolerance: Double): Boolean = {
      // This is a slope comparison.. Any point on the line should have the same slope as the line.
      val m2: Double =
        if (vertex.x === 0) 0
        else (b - vertex.y) / (0 - vertex.x)

      val mDelta: Double =
        m - m2

      mDelta >= -tolerance && mDelta <= tolerance
    }

  }
  final case class ParallelToAxisY(xPosition: Double) extends Line
  case object InvalidLine                             extends Line

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  def fromLineSegment(lineSegment: LineSegment): Line =
    (lineSegment.start, lineSegment.end) match {
      case (Vertex(x1, y1), Vertex(x2, y2)) if x1 === x2 && y1 === y2 =>
        Line.InvalidLine

      case (Vertex(x1, _), Vertex(x2, _)) if x1 === x2 =>
        Line.ParallelToAxisY(x1)

      case (Vertex(x1, y1), Vertex(x2, y2)) =>
        val m: Double = (y2 - y1) / (x2 - x1)

        Line.Components(m, y1 - (m * x1))
    }

  def intersection(l1: Line, l2: Line): LineIntersectionResult =
    /*
    y-intercept = mx + b (i.e. y = mx + b)
    x-intercept = -b/m   (i.e. x = -b/m where y is moved to 0)
     */
    (l1, l2) match {
      case (Line.Components(m1, _), Line.Components(m2, _)) if m1 === m2 =>
        // Same slope, so parallel
        LineIntersectionResult.NoIntersection

      case (Line.Components(m1, b1), Line.Components(m2, b2)) =>
        //x = -b/m
        val x: Double = (b2 - b1) / (m1 - m2)

        //y = mx + b
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
    }

}

sealed trait LineIntersectionResult {
  def toOption: Option[Vertex]
  def toList: List[Vertex]
}
object LineIntersectionResult {
  final case class IntersectionVertex(x: Double, y: Double) extends LineIntersectionResult {
    def toVertex: Vertex =
      Vertex(x, y)

    def toOption: Option[Vertex] =
      Some(toVertex)

    def toList: List[Vertex] =
      List(toVertex)
  }
  case object NoIntersection extends LineIntersectionResult {
    def toOption: Option[Vertex] = None
    def toList: List[Vertex]     = Nil
  }
}
