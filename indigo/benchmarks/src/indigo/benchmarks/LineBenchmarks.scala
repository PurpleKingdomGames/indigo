package indigo.benchmarks

import indigo.*
import japgolly.scalajs.benchmark.*
import japgolly.scalajs.benchmark.gui.*

object LineBenchmarks:

  val lineSegParallel1 = LineSegment(Vertex(0, 0), Vertex(10, 0))
  val lineSegParallel2 = LineSegment(Vertex(0, 2), Vertex(10, 2))
  val lineParallel1    = LineSegment(Vertex(0, 0), Vertex(10, 0)).toLine
  val lineParallel2    = LineSegment(Vertex(0, 2), Vertex(10, 2)).toLine

  val lineSegDiagonal1 = LineSegment(Vertex(0, 0), Vertex(10, 10))
  val lineSegDiagonal2 = LineSegment(Vertex(10, 0), Vertex(0, 10))
  val lineDiagonal1    = LineSegment(Vertex(0, 0), Vertex(10, 10)).toLine
  val lineDiagonal2    = LineSegment(Vertex(10, 0), Vertex(0, 10)).toLine

  val lineSegSquare1 = LineSegment(Vertex(-3, 0), Vertex(3, 0))
  val lineSegSquare2 = LineSegment(Vertex(0, -3), Vertex(0, 3))
  val lineSquare1    = LineSegment(Vertex(-3, 0), Vertex(3, 0)).toLine
  val lineSquare2    = LineSegment(Vertex(0, -3), Vertex(0, 3)).toLine

  val facingVertex = Vertex(10, 0)

  val suite = GuiSuite(
    Suite("Line Benchmarks")(
      Benchmark("LineSegment to Line") {
        lineSegDiagonal1.toLine
      },
      Benchmark("LineSegment is facing vertex") {
        lineSegDiagonal1.isFacingVertex(facingVertex)
      },
      Benchmark("LineSegments are parallel (intersects with)") {
        lineSegParallel1.intersectsWith(lineSegParallel2)
      },
      Benchmark("LineSegments are parallel (intersects at)") {
        lineSegParallel1.intersectsAt(lineSegParallel2)
      },
      Benchmark("LineSegments diagonally intersect (intersects with)") {
        lineSegDiagonal1.intersectsWith(lineSegDiagonal2)
      },
      Benchmark("LineSegments diagonally intersect (intersects at)") {
        lineSegDiagonal1.intersectsAt(lineSegDiagonal2)
      },
      Benchmark("LineSegments squarely intersect (intersects with)") {
        lineSegSquare1.intersectsWith(lineSegSquare2)
      },
      Benchmark("LineSegments squarely intersect (intersects at)") {
        lineSegSquare1.intersectsAt(lineSegSquare2)
      },
      Benchmark("Lines are parallel (intersects with)") {
        lineParallel1.intersectsWith(lineParallel2)
      },
      Benchmark("Lines are parallel (intersects at)") {
        lineParallel1.intersectsAt(lineParallel2)
      },
      Benchmark("Lines diagonally intersect (intersects with)") {
        lineDiagonal1.intersectsWith(lineDiagonal2)
      },
      Benchmark("Lines diagonally intersect (intersects at)") {
        lineDiagonal1.intersectsAt(lineDiagonal2)
      },
      Benchmark("Lines squarely intersect (intersects with)") {
        lineSquare1.intersectsWith(lineSquare2)
      },
      Benchmark("Lines squarely intersect (intersects at)") {
        lineSquare1.intersectsAt(lineSquare2)
      }
    )
  )
