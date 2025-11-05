package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object Collisions:

  val pointInside  = Point(5)
  val pointOutside = Point(20)

  val vertexInside  = Vertex(5)
  val vertexOutside = Vertex(20)

  val rectangle               = Rectangle(0, 0, 10, 10)
  val overlappingRectangle    = Rectangle(5, 5, 10, 10)
  val nonOverlappingRectangle = Rectangle(20, 20, 10, 10)

  val boundingBox               = BoundingBox(0, 0, 10, 10)
  val overlappingBoundingBox    = BoundingBox(5, 5, 10, 10)
  val nonOverlappingBoundingBox = BoundingBox(20, 20, 10, 10)

  val lineIntersects       = LineSegment(Vertex(-1, -1), Vertex(11, 11))
  val lineDoesNotIntersect = LineSegment(Vertex(20, 20), Vertex(31, 31))

  val suite = GuiSuite(
    Suite("Collision Benchmarks")(
      Benchmark("Rectangle contains Point (hit)") {
        rectangle.contains(pointInside)
      },
      Benchmark("Rectangle contains Point (miss)") {
        rectangle.contains(pointOutside)
      },
      Benchmark("Rectangle contains Point (hit - with allocations)") {
        Rectangle(0, 0, 10, 10).contains(Point(5))
      },
      Benchmark("Rectangle contains Point (miss - with allocations)") {
        Rectangle(0, 0, 10, 10).contains(Point(20))
      },
      Benchmark("Rectangles overlapping") {
        rectangle.overlaps(overlappingRectangle)
      },
      Benchmark("Rectangles non-overlapping") {
        rectangle.overlaps(nonOverlappingRectangle)
      },
      Benchmark("BoundingBox contains Vertex (hit)") {
        boundingBox.contains(vertexInside)
      },
      Benchmark("BoundingBox contains Vertex (miss)") {
        boundingBox.contains(vertexOutside)
      },
      Benchmark("BoundingBox overlapping") {
        boundingBox.overlaps(overlappingBoundingBox)
      },
      Benchmark("BoundingBox non-overlapping") {
        boundingBox.overlaps(nonOverlappingBoundingBox)
      },
      Benchmark("BoundingBox line intersects") {
        boundingBox.lineIntersects(lineIntersects)
      },
      Benchmark("BoundingBox line doesn't intersect") {
        boundingBox.lineIntersects(lineDoesNotIntersect)
      },
      Benchmark("BoundingBox line intersects at") {
        boundingBox.lineIntersectsAt(lineIntersects)
      },
      Benchmark("BoundingBox line doesn't intersect anywhere") {
        boundingBox.lineIntersectsAt(lineDoesNotIntersect)
      }
    )
  )
