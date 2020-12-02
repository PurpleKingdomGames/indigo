package snake.model.quadtrees

import indigo.shared.PowerOfTwo
import indigo.shared.datatypes.{Point, Rectangle}
import indigo.shared.EqualTo._
import snake.model.grid.GridPoint
import snake.model.quadtrees.QuadTree.{QuadBranch, QuadEmpty, QuadLeaf}

class QuadTreeTests extends munit.FunSuite {

  test("should be able to fetch an element at a given position") {
    val gridPoint: GridPoint = GridPoint(5, 1)

    val tree: QuadBranch[String] = QuadBranch(
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

    assertEquals(tree.fetchElementAt(gridPoint), Some("hello"))

  }

  val tree = QuadTree
    .empty(PowerOfTwo._16)
    .insertElement("a", GridPoint(9, 2))
    .insertElement("b", GridPoint(0, 0))
    .insertElement("c", GridPoint(10, 10))
    .insertElement("d", GridPoint(20, 50))

  test("should be able to insert an element at a given position.[9, 2]") {
    assertEquals(tree.fetchElementAt(GridPoint(9, 2)), Some("a"))
  }

  test("should be able to insert an element at a given position.Should be missing at [1, 2]") {
    assertEquals(tree.fetchElementAt(GridPoint(1, 2)), None)
  }

  test("should be able to insert an element at a given position.[0, 0]") {
    assertEquals(tree.fetchElementAt(GridPoint(0, 0)), Some("b"))
  }

  test("should be able to insert an element at a given position.[10, 10]") {
    assertEquals(tree.fetchElementAt(GridPoint(10, 10)), Some("c"))
  }

  test("should be able to insert an element at a given position.Outside of area at [20, 50]") {
    assertEquals(tree.fetchElementAt(GridPoint(20, 50)), None)
  }

  test("should be able to remove an element from a tree") {

    val gridPoint = GridPoint(9, 2)

    val tree = QuadTree
      .empty(PowerOfTwo._16)
      .insertElement("test", gridPoint)

    assertEquals(tree.fetchElementAt(gridPoint), Some("test"))

    val tree2 = tree.removeElement(gridPoint)

    assertEquals(tree2.fetchElementAt(gridPoint), None)

  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty(PowerOfTwo._2)
      .insertElement("a", GridPoint(0, 0))
      .insertElement("b", GridPoint(0, 1))
      .insertElement("c", GridPoint(1, 0))

    val treeB = QuadTree
      .empty(PowerOfTwo._2)
      .insertElement("a", GridPoint(0, 0))
      .insertElement("b", GridPoint(0, 1))
      .insertElement("c", GridPoint(1, 0))

    assertEquals(treeA === treeB, true)
  }

  test("should be able to check equality.not equal") {

    val treeA = QuadTree
      .empty(PowerOfTwo._2)
      .insertElement("a", GridPoint(0, 0))
      .insertElement("b", GridPoint(0, 1))
      .insertElement("c", GridPoint(1, 0))

    val treeB = QuadTree
      .empty(PowerOfTwo._2)
      .insertElement("a", GridPoint(0, 0))
      .insertElement("b", GridPoint(0, 1))
      .insertElement("d", GridPoint(1, 0))

    assertEquals(treeA === treeB, false)
  }

  test("should be able to prune an existing tree to simplify the structure") {

    val gridPoint = GridPoint(9, 2)

    val tree = QuadTree
      .empty(PowerOfTwo._16)
      .insertElement(999, gridPoint)
      .removeElement(gridPoint)
      .prune

    assertEquals(tree === QuadTree.empty(PowerOfTwo._16), true)

  }

  test("should not prune an already optimal tree") {

    val gridPoint = GridPoint(9, 2)

    val tree = QuadTree
      .empty(PowerOfTwo._16)
      .insertElement(999, gridPoint)

    assertEquals(tree.prune, tree)

  }

  test("should be able to search for a leaf under a point") {
    val tree = QuadTree
      .empty(PowerOfTwo._2)
      .insertElement("a", GridPoint(0, 0))
      .insertElement("b", GridPoint(0, 1))
      .insertElement("c", GridPoint(1, 0))

    val expected: List[String] = List("b")

    val point: Point = Point(0, 1)

    assertEquals(QuadTree.searchByPoint(tree, point), expected)
  }

  test("should allow a search of squares where the line points are in the same square") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Point(1, 1), Point(1, 1))

    val expected: List[String] =
      List(
        "1,1"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected, actual)
  }

  test("should allow a search of squares between two horizontal points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Point(1, 1), Point(3, 1))

    val expected: List[String] =
      List(
        "1,1",
        "2,1",
        "3,1"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected, actual)
  }

  test("should allow a search of squares between two vertical points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Point(2, 0), Point(2, 2))

    val expected: List[String] =
      List(
        "2,0",
        "2,1",
        "2,2"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected, actual)
  }

  test("should allow a search of squares between two 45 degree points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Point(0, 0), Point(3, 3))

    val expected: List[String] =
      List(
        "0,0",
        "1,1",
        "2,2",
        "3,3"
      )

    assertEquals(expected.forall(p => actual.contains(p)), true)

    // This is not an exact search. We know intuitively that all the points in "expected" must
    // be there, however, due to floating points and tolerances and what-not, we could get other
    // hits that are very near by, and that is kind of ok. The expectation is that our game
    // coder will be doing further checks on this reduced list if they want greater accuracy.
    assert(actual.length <= expected.length + 2)
  }

  /*
     0 1 2 3
     _ _ _ _
  0 |_|_|_|_|
  1 |s|_|_|_|
  2 |_|_|_|e|
  3 |_|_|_|_|
   */
  test("should allow a search of squares between two diagonal degree points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Point(0, 1), Point(3, 2))

    val expected: List[String] =
      List(
        "0,1",
        "1,1",
        "2,1",
        "2,2",
        "3,2"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected.forall(p => actual.contains(p)), true)
  }

  test("should allow a search of squares intersecting with a 1x1 rectangle") {
    val r: Rectangle = Rectangle(1, 1, 1, 1)

    val actual = QuadTree.searchByRectangle(SampleTree.tree, r)

    val expected: List[String] = List("1,1")

    assertEquals(actual.length, expected.length)
    assertEquals(expected.forall(p => actual.contains(p)), true)
  }

  test("should allow a search of squares intersecting with a 2x2 rectangle") {
    val r: Rectangle = Rectangle(0, 1, 2, 2)

    val actual = QuadTree.searchByRectangle(SampleTree.tree, r)

    val expected: List[String] = List(
      "0,1",
      "1,1",
      "0,2",
      "1,2"
    )

    assertEquals(actual.length, expected.length)
    assertEquals(expected.forall(p => actual.contains(p)), true)
  }

  test("should allow a search of squares intersecting with a rectangle the size of the grid") {
    val r: Rectangle = Rectangle(0, 0, 4, 4)

    val actual = QuadTree.searchByRectangle(SampleTree.tree, r)

    val expected: List[String] =
      List(
        "0,0",
        "0,1",
        "0,2",
        "0,3",
        "1,0",
        "1,1",
        "1,2",
        "1,3",
        "2,0",
        "2,1",
        "2,2",
        "2,3",
        "3,0",
        "3,1",
        "3,2",
        "3,3"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected.forall(p => actual.contains(p)), true)
  }

  test("should allow a search of squares intersecting with a rectangle") {
    val r: Rectangle = Rectangle(0, 1, 4, 2)

    val actual = QuadTree.searchByRectangle(SampleTree.tree, r)

    val expected: List[String] =
      List(
        "0,1",
        "1,1",
        "2,1",
        "3,1",
        "0,2",
        "1,2",
        "2,2",
        "3,2"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(expected.forall(p => actual.contains(p)), true)
  }

}

object SampleTree {

  val tree: QuadTree[String] = QuadTree
    .empty(PowerOfTwo._4)
    .insertElement("0,0", GridPoint(0, 0))
    .insertElement("0,1", GridPoint(0, 1))
    .insertElement("0,2", GridPoint(0, 2))
    .insertElement("0,3", GridPoint(0, 3))
    .insertElement("1,0", GridPoint(1, 0))
    .insertElement("1,1", GridPoint(1, 1))
    .insertElement("1,2", GridPoint(1, 2))
    .insertElement("1,3", GridPoint(1, 3))
    .insertElement("2,0", GridPoint(2, 0))
    .insertElement("2,1", GridPoint(2, 1))
    .insertElement("2,2", GridPoint(2, 2))
    .insertElement("2,3", GridPoint(2, 3))
    .insertElement("3,0", GridPoint(3, 0))
    .insertElement("3,1", GridPoint(3, 1))
    .insertElement("3,2", GridPoint(3, 2))
    .insertElement("3,3", GridPoint(3, 3))

}
