package snake

import org.scalatest.{FunSpec, Matchers}

class GameMapSpec extends FunSpec with Matchers {

  describe("QuadTrees") {
    pending
  }

  describe("QuadBounds") {
    pending
  }

  describe("GridPoints") {
    pending
  }

  describe("MapElements") {

    it("should allow folding of a list of MapElements") {

      val result = List(MapElement.Empty(GridPoint.identity), MapElement.Apple(GridPoint.identity), MapElement.Wall(GridPoint.identity)).foldLeft(MapElement.identity)(_ + _)

      result shouldEqual MapElement.Apple(GridPoint.identity)

    }

  }

}
