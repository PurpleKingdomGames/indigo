package indigoexts.line

import indigo.gameengine.scenegraph.datatypes.Point
import indigoexts.line.IntersectionResult._
import indigoexts.line.LineProperties._
import org.scalatest.{FunSpec, Matchers}

class LineSegmentSpec extends FunSpec with Matchers {

  /*
  y = mx + b

  We're trying to calculate m and b where
  m is the slope i.e. number of y units per x unit
  b is the y-intersect i.e. the point on the y-axis where the line passes through it
   */
  describe("calculating line components") {

    it("should correctly calculate the line components") {

      withClue("(0, 0) -> (2, 2)") {
        val expected: LineComponents = LineComponents(1, 0)

        LineSegment.calculateLineComponents(Point(0, 0), Point(2, 2)) shouldEqual expected
      }

      withClue("(2, 1) -> (3, 4)") {
        val expected: LineComponents = LineComponents(3, -5)

        LineSegment.calculateLineComponents(Point(2, 1), Point(3, 4)) shouldEqual expected
      }

      withClue("(2, 2) -> (2, -3)") {
        // Does not work because you can't have m of 0 or b of infinity)
        // i.e. lines parallel to x or y axis
        // We're also getting a divide by 0 because ... / x - x = 0
        val expected = ParallelToAxisY

        LineSegment.calculateLineComponents(Point(2, 2), Point(2, -3)) shouldEqual expected
      }

    }

    it("should correctly identify a line parallel to the x-axis") {
      //m = 0
      LineSegment.calculateLineComponents(Point(-1, 2), Point(1, 2)) shouldEqual ParallelToAxisX
    }

    it("should correctly identify a line parallel to the y-axis") {
      //b = Infinity (or -Infinity)
      LineSegment.calculateLineComponents(Point(1, 2), Point(1, -3)) shouldEqual ParallelToAxisY
    }

  }

  describe("line intersections") {

    it("should not intersect with a parallel lines") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((-3, 3), (2, 3)),
        LineSegment((1, 1), (-2, 1))
      )

      val expected = NoIntersection

      actual shouldEqual expected
    }

    it("should intersect lines at right angles to each other") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((2, 2), (2, -3)),
        LineSegment((-1, -2), (3, -2))
      )

      val expected: IntersectionPoint = IntersectionPoint(2, -2)

      actual shouldEqual expected
    }

    it("should intersect diagonally right angle lines") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((1, 1), (5, 5)),
        LineSegment((1, 5), (4, 2))
      )

      val expected: IntersectionPoint = IntersectionPoint(3, 3)

      actual shouldEqual expected
    }

    it("should intersect diagonally non-right angle lines") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((1, 5), (3, 1)),
        LineSegment((1, 2), (4, 5))
      )

      val expected: IntersectionPoint = IntersectionPoint(2, 3)

      actual shouldEqual expected
    }

    it("should intersect where one line is parallel to the y-axis") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((4, 1), (4, 4)),
        LineSegment((2, 1), (5, 4))
      )

      val expected: IntersectionPoint = IntersectionPoint(4, 3)

      actual shouldEqual expected
    }

    it("should intersect where one line is parallel to the x-axis") {
      val actual: IntersectionResult = LineSegment.intersection(
        LineSegment((1, 2), (5, 2)),
        LineSegment((2, 4), (5, 1))
      )

      val expected: IntersectionPoint = IntersectionPoint(4, 2)

      actual shouldEqual expected
    }

    it("should give the same intersection regardless of order") {
      val actual1: IntersectionResult = LineSegment.intersection(
        LineSegment((0, 15), (50, 15)),
        LineSegment((10, 10), (10, 30))
      )

      val actual2: IntersectionResult = LineSegment.intersection(
        LineSegment((0, 15), (50, 15)),
        LineSegment((10, 10), (10, 30))
      )

      val expected: IntersectionPoint = IntersectionPoint(10, 15)

      actual1 shouldEqual expected
      actual2 shouldEqual expected
      actual1 shouldEqual actual2
    }

  }

  describe("normals") {
    it("should calculate the normal for a horizontal line (Left -> Right)") {
      val start: Point = Point(-10, 1)
      val end: Point   = Point(10, 1)

      LineSegment.calculateNormal(start, end) shouldEqual Point(0, 1)
    }

    it("should calculate the normal for a horizontal line (Right -> Left)") {
      val start: Point = Point(5, 2)
      val end: Point   = Point(-5, 2)

      LineSegment.calculateNormal(start, end) shouldEqual Point(0, -1)
    }

    it("should calculate the normal for a vertical line (Top -> Bottom") {
      val start: Point = Point(-1, 10)
      val end: Point   = Point(-1, -10)

      LineSegment.calculateNormal(start, end) shouldEqual Point(1, 0)
    }

    it("should calculate the normal for a vertical line (Bottom -> Top") {
      val start: Point = Point(1, -10)
      val end: Point   = Point(1, 10)

      LineSegment.calculateNormal(start, end) shouldEqual Point(-1, 0)
    }

    it("should calculate the normal for a diagonal line") {
      val start: Point = Point(2, 2)
      val end: Point   = Point(-2, -2)

      LineSegment.calculateNormal(start, end) shouldEqual Point(1, -1)
    }

  }

  describe("Normalising a point") {

    it("should be able to normalise a point") {

      withClue("10, 10") {
        LineSegment.normalisePoint(Point(10, 10)) shouldEqual Point(1, 1)
      }

      withClue("-10, -10") {
        LineSegment.normalisePoint(Point(-10, -10)) shouldEqual Point(-1, -1)
      }

      withClue("10, 0") {
        LineSegment.normalisePoint(Point(10, 0)) shouldEqual Point(1, 0)
      }

      withClue("0, 10") {
        LineSegment.normalisePoint(Point(0, 10)) shouldEqual Point(0, 1)
      }

      withClue("-50, 1000") {
        LineSegment.normalisePoint(Point(-50, 1000)) shouldEqual Point(-1, 1)
      }

    }

  }

  describe("Point on a line") {

    //TODO: Can do a property based check here. Forall points on a line
    // (i.e. start point * slope m < end point)
    it("should be able to check if a point is on a line") {
      withClue("horizontal") {
        val line: LineSegment = LineSegment((10, 10), (20, 10))
        val point: Point      = Point(15, 10)

        LineSegment.lineContainsPoint(line, point) shouldEqual true
      }

      withClue("vertical") {
        val line: LineSegment = LineSegment((10, 10), (10, 20))
        val point: Point      = Point(10, 15)

        LineSegment.lineContainsPoint(line, point) shouldEqual true
      }

      withClue("diagonal") {
        val line: LineSegment = LineSegment((10, 10), (20, 20))
        val point: Point      = Point(15, 15)

        LineSegment.lineContainsPoint(line, point) shouldEqual true
      }
    }

    it("should be able to check if a point is NOT on a line") {
      val line: LineSegment = LineSegment((10, 10), (20, 20))
      val point: Point      = Point(1, 5)

      LineSegment.lineContainsPoint(line, point) shouldEqual false
    }

  }

}
