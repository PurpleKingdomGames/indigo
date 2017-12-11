package snake

import org.scalatest.{FunSpec, Matchers}

class GameMapSpec extends FunSpec with Matchers {

  describe("QuadTrees") {

    it("should be able to flatten a QuadTree") {
      pending
    }

    it("should be able to append two QuadTrees") {
      pending
    }

    it("should be able to fetch an element at a given position") {
      pending
    }

    it("should be able to insert an element at a given position") {
      pending
    }

  }

  describe("QuadBounds") {

    it("should allow two bounds to be appended") {

      val a = QuadBounds(0, 0, 100, 100)
      val b = QuadBounds(50, 50, 100, 100)

      a + b shouldEqual QuadBounds(0, 0, 150, 150)
    }

    it("should be about to check a point is within the bounds") {

      val b = QuadBounds(0, 0, 10, 10)

      b.isPointWithinBounds(GridPoint(5, 5)) shouldEqual true
      b.isPointWithinBounds(GridPoint(0, 0)) shouldEqual true
      b.isPointWithinBounds(GridPoint(-1, 5)) shouldEqual false
      b.isPointWithinBounds(GridPoint(5, 20)) shouldEqual false

    }

  }

  describe("MapElements") {

    it("should allow folding of a list of MapElements") {

      val result = List(MapElement.Empty(GridPoint.identity), MapElement.Apple(GridPoint.identity), MapElement.Wall(GridPoint.identity)).foldLeft(MapElement.identity)(_ + _)

      result shouldEqual MapElement.Apple(GridPoint.identity)

    }

  }

}
