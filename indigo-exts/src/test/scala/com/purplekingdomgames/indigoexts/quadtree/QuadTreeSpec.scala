package com.purplekingdomgames.indigoexts.quadtree

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.{Point, Rectangle}
import com.purplekingdomgames.indigoexts.grid.GridPoint
import com.purplekingdomgames.indigoexts.quadtree.QuadTree.{QuadBranch, QuadEmpty, QuadLeaf}
import org.scalactic.Equality
import org.scalatest.{FunSpec, Matchers}

class QuadTreeSpec extends FunSpec with Matchers {

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
            QuadLeaf(QuadBounds(5, 1, 1, 1), "hello")
          ),
          QuadEmpty(QuadBounds(6, 0, 2, 2)),
          QuadEmpty(QuadBounds(4, 2, 2, 2)),
          QuadEmpty(QuadBounds(6, 2, 2, 2))
        ),
        QuadEmpty(QuadBounds(0, 4, 4, 4)),
        QuadEmpty(QuadBounds(4, 4, 4, 4))
      )

      tree.fetchElementAt(gridPoint) shouldEqual Some("hello")

    }

    it("should be able to insert an element at a given position") {

      val tree = QuadTree
        .empty(16)
        .insertElement("a", GridPoint(9, 2))
        .insertElement("b", GridPoint(0, 0))
        .insertElement("c", GridPoint(10, 10))
        .insertElement("d", GridPoint(20, 50))

      withClue("[9, 2]") {
        tree.fetchElementAt(GridPoint(9, 2)) shouldEqual Some("a")
      }

      withClue("Should be missing at [1, 2]") {
        tree.fetchElementAt(GridPoint(1, 2)) shouldEqual None
      }

      withClue("[0, 0]") {
        tree.fetchElementAt(GridPoint(0, 0)) shouldEqual Some("b")
      }

      withClue("[10, 10]") {
        tree.fetchElementAt(GridPoint(10, 10)) shouldEqual Some("c")
      }

      withClue("Outside of area at [20, 50]") {
        tree.fetchElementAt(GridPoint(20, 50)) shouldEqual None
      }

    }

    it("should be able to remove an element from a tree") {

      val gridPoint = GridPoint(9, 2)

      val tree = QuadTree
        .empty(16)
        .insertElement("test", gridPoint)

      tree.fetchElementAt(gridPoint) shouldEqual Some("test")

      val tree2 = tree.removeElement(gridPoint)

      tree2.fetchElementAt(gridPoint) shouldEqual None

    }

    // Needed because of the funky QuadBounds type.
    implicit def eq[T]: Equality[QuadTree[T]] =
      new Equality[QuadTree[T]] {
        def areEqual(a: QuadTree[T], b: Any): Boolean =
          (a, b) match {
            case (QuadEmpty(b1), QuadEmpty(b2)) if b1 === b2 =>
              true

            case (QuadLeaf(b1, v1), QuadLeaf(b2, v2)) if b1 === b2 && v1 == v2 =>
              true

            case (QuadBranch(bounds1, a1, b1, c1, d1), QuadBranch(bounds2, a2, b2, c2, d2)) =>
              bounds1 === bounds2 && areEqual(a1, a2) && areEqual(b1, b2) && areEqual(c1, c2) && areEqual(d1, d2)

            case _ =>
              false
          }
      }

    it("should be able to prune an existing tree to simplify the structure") {

      val gridPoint = GridPoint(9, 2)

      val tree = QuadTree
        .empty(16)
        .insertElement(999, gridPoint)
        .removeElement(gridPoint)
        .prune

      tree shouldEqual QuadTree.empty(16)

    }

    it("should not prune an already optimal tree") {

      val gridPoint = GridPoint(9, 2)

      val tree = QuadTree
        .empty(16)
        .insertElement(999, gridPoint)

      tree.prune shouldEqual tree

    }

    it("should be able to search for leaves matching a predicate") {
      val tree = QuadTree
        .empty(2)
        .insertElement("a", GridPoint(0, 0))
        .insertElement("b", GridPoint(0, 1))
        .insertElement("c", GridPoint(1, 0))

      val expected: List[String] = List("b", "c")

      val predicate: QuadBounds => Boolean = { b =>
        println(b.renderAsString)
        b.x > 0 || b.y > 0
      }

      tree.search(predicate) shouldEqual expected

      // withClue("2x2") {
      //   val tree = QuadTree
      //     .empty(2)
      //     .insertElement("a", GridPoint(0, 0))
      //     .insertElement("b", GridPoint(0, 1))
      //     .insertElement("c", GridPoint(1, 0))
      //
      //   val expected: List[String] = List("a", "b")
      //
      //   val within: Rectangle = Rectangle(0, 0, 0, 1)
      //
      //   val predicate: QuadBounds => Boolean = { bounds =>
      //     bounds.toRectangle.intersects(within)
      //   }
      //
      //   tree.search(predicate) shouldEqual expected
      // }

      // withClue("4x4") {
      //   val tree = QuadTree
      //     .empty(4)
      //     .insertElement("0,0", GridPoint(0, 0))
      //     .insertElement("0,1", GridPoint(0, 1))
      //     .insertElement("0,2", GridPoint(0, 2))
      //     .insertElement("0,3", GridPoint(0, 3))
      //     .insertElement("1,0", GridPoint(1, 0))
      //     .insertElement("1,1", GridPoint(1, 1))
      //     .insertElement("1,2", GridPoint(1, 2))
      //     .insertElement("1,3", GridPoint(1, 3))
      //     .insertElement("2,0", GridPoint(2, 0))
      //     .insertElement("2,1", GridPoint(2, 1))
      //     .insertElement("2,2", GridPoint(2, 2))
      //     .insertElement("2,3", GridPoint(2, 3))
      //     .insertElement("3,0", GridPoint(3, 0))
      //     .insertElement("3,1", GridPoint(3, 1))
      //     .insertElement("3,2", GridPoint(3, 2))
      //     .insertElement("3,3", GridPoint(3, 3))
      //
      //   val expected: List[String] = List(
      //     "0,1",
      //     "1,1",
      //     "2,1",
      //     "3,1",
      //     "0,2",
      //     "1,2",
      //     "2,2",
      //     "3,2"
      //   )
      //
      //   val searchRectangle: Rectangle = Rectangle(0, 1, 3, 2)
      //
      //   val predicate: QuadBounds => Boolean = { bounds =>
      //     val refPoint =
      //       if (bounds.width <= 1 || bounds.height <= 1) Point(bounds.x, bounds.y)
      //       else bounds.center.toPoint
      //
      //     val p: Boolean = searchRectangle.isPointWithin(refPoint)
      //
      //     println("refPoint: " + refPoint + " : " + bounds.renderAsString + " - " + p.toString)
      //     // println(p)
      //     // bounds.toRectangle.intersects(searchRectangle)
      //     p
      //   }
      //
      //   val actual = tree.search(predicate)
      //
      //   actual.length shouldEqual expected.length
      //   expected.forall(p => actual.contains(p)) shouldEqual true
      //
      // }
    }

    it("should allow a search of squares between two points") {
      pending
    }

    it("should allow a search of squares between intersecting with a rectangle") {
      pending
    }

    it("should allow a search of squares fully enclosed in a rectangle") {
      pending
    }

  }

  describe("QuadBounds") {

    it("should be able to check a point is within the bounds") {

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
