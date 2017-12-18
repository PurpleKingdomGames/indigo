package snake

import org.scalatest.{FunSpec, Matchers}
import snake.QuadTree.{QuadBranch, QuadEmpty, QuadLeaf}

class GameMapSpec extends FunSpec with Matchers {

  describe("QuadTrees") {

    it("should be able to fetch an element at a given position") {
      val gridPoint: GridPoint = GridPoint(5, 1)

      val tree = QuadBranch(
        QuadBounds(0, 0, 8, 8),
        QuadEmpty(QuadBounds(0, 0, 4, 4)),
        QuadBranch(
          QuadBounds(4, 0, 4, 4),
          QuadBranch(
            QuadBounds(4, 0, 2, 2),
            QuadEmpty(QuadBounds(4, 0, 1, 1)),
            QuadEmpty(QuadBounds(5, 0, 1, 1)),
            QuadEmpty(QuadBounds(4, 1, 1, 1)),
            QuadLeaf(QuadBounds(5, 1, 1, 1), MapElement.Apple(gridPoint))
          ),
          QuadEmpty(QuadBounds(6, 0, 2, 2)),
          QuadEmpty(QuadBounds(4, 2, 2, 2)),
          QuadEmpty(QuadBounds(6, 2, 2, 2))
        ),
        QuadEmpty(QuadBounds(0, 4, 4, 4)),
        QuadEmpty(QuadBounds(4, 4, 4, 4)),
      )

      tree.fetchElementAt(gridPoint) shouldEqual Some(MapElement.Apple(gridPoint))

    }

    it("should be able to insert an element at a given position") {

      val tree = QuadTree.empty(16)
        .insertElement(MapElement.Apple(GridPoint(9, 2)))
        .insertElement(MapElement.Wall(GridPoint(0, 0)))
        .insertElement(MapElement.Wall(GridPoint(10, 10)))
        .insertElement(MapElement.Apple(GridPoint(20, 50)))

      withClue("[9, 2]") {
        tree.fetchElementAt(GridPoint(9, 2)) shouldEqual Some(MapElement.Apple(GridPoint(9, 2)))
      }

      withClue("Should be missing at [1, 2]") {
        tree.fetchElementAt(GridPoint(1, 2)) shouldEqual None
      }

      withClue("[0, 0]") {
        tree.fetchElementAt(GridPoint(0, 0)) shouldEqual Some(MapElement.Wall(GridPoint(0, 0)))
      }

      withClue("[10, 10]") {
        tree.fetchElementAt(GridPoint(10, 10)) shouldEqual Some(MapElement.Wall(GridPoint(10, 10)))
      }

      withClue("Outside of area at [20, 50]") {
        tree.fetchElementAt(GridPoint(20, 50)) shouldEqual None
      }

    }

  }

  describe("QuadBounds") {

    it("should be about to check a point is within the bounds") {

      val b = QuadBounds(0, 0, 10, 10)

      b.isPointWithinBounds(GridPoint(5, 5)) shouldEqual true
      b.isPointWithinBounds(GridPoint(0, 0)) shouldEqual true
      b.isPointWithinBounds(GridPoint(-1, 5)) shouldEqual false
      b.isPointWithinBounds(GridPoint(5, 20)) shouldEqual false

    }

    it("should be able to subdivide") {

      val b = QuadBounds(0, 0, 10, 10)

      b.subdivide._1 === QuadBounds(0, 0, 5, 5) shouldEqual true
      b.subdivide._2 === QuadBounds(5, 0, 5, 5) shouldEqual true
      b.subdivide._3 === QuadBounds(0, 5, 5, 5) shouldEqual true
      b.subdivide._4 === QuadBounds(5, 5, 5, 5) shouldEqual true

    }

    it("should be able to re-combine") {
      val original = QuadBounds(0, 0, 0, 2)

      val divisions = original.subdivide

      val recombined =
        QuadBounds.combine(divisions._1, List(divisions._2, divisions._3, divisions._4))

      recombined === original shouldEqual true
    }

  }

}
