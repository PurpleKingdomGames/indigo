package indigoextras.trees

import indigo.shared.collections.Batch
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex
import indigoextras.trees.QuadTree.QuadBranch
import indigoextras.trees.QuadTree.QuadEmpty
import indigoextras.trees.QuadTree.QuadLeaf
import indigo.shared.datatypes.Point

class QuadTreeTests extends munit.FunSuite {

  test("should be able to fetch an element at a given position") {
    val gridPoint: Vertex = Vertex(5, 1)

    val tree: QuadBranch[Vertex, String] = QuadBranch(
      BoundingBox(0, 0, 8, 8),
      QuadEmpty(BoundingBox(0, 0, 4, 4)),
      QuadBranch(
        BoundingBox(4, 0, 4, 4),
        QuadBranch(
          BoundingBox(4, 0, 2, 2),
          QuadEmpty(BoundingBox(4, 0, 1, 1)),
          QuadEmpty(BoundingBox(5, 0, 1, 1)),
          QuadEmpty(BoundingBox(4, 1, 1, 1)),
          QuadLeaf(BoundingBox(5, 1, 1, 1), Vertex(5, 1), "hello")
        ),
        QuadEmpty(BoundingBox(6, 0, 2, 2)),
        QuadEmpty(BoundingBox(4, 2, 2, 2)),
        QuadEmpty(BoundingBox(6, 2, 2, 2))
      ),
      QuadEmpty(BoundingBox(0, 4, 4, 4)),
      QuadEmpty(BoundingBox(4, 4, 4, 4))
    )

    assertEquals(tree.fetchElement(gridPoint), Some("hello"))

  }

  val tree = QuadTree
    .empty(16, 16)
    .insertElement(Vertex(9, 2), "a")
    .insertElement(Vertex(0, 0), "b")
    .insertElement(Vertex(10, 10), "c")
    .insertElement(Vertex(20, 50), "d")

  test("should be able insert multiple items") {
    given CanEqual[Option[String], Option[String]] = CanEqual.derived

    val actual =
      QuadTree(
        (Vertex(9, 2), "a"),
        (Vertex(0, 0), "b"),
        (Vertex(10, 10), "c")
      )

    val expected =
      QuadBranch(
        BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
        QuadLeaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Vertex(0, 0), "b"),
        QuadLeaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Vertex(9, 2), "a"),
        QuadEmpty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
        QuadLeaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Vertex(10, 10), "c")
      )

    assertEquals(actual, expected)

    assert(
      List(Vertex(9, 2), Vertex(0, 0), Vertex(10, 10)).forall { v =>
        clue(tree.fetchElement(clue(v))) == actual.fetchElement(v) &&
        tree.fetchElement(v) == expected.fetchElement(v)
      }
    )
  }

  test("should be able to insert an element at a given position.[9, 2]") {
    assertEquals(tree.fetchElement(Vertex(9, 2)), Some("a"))
  }

  test("should be able to insert an element at a given position.Should be missing at [1, 2]") {
    assertEquals(tree.fetchElement(Vertex(1, 2)), None)
  }

  test("should be able to insert an element at a given position.[0, 0]") {
    assertEquals(tree.fetchElement(Vertex(0, 0)), Some("b"))
  }

  test("should be able to insert an element at a given position.[10, 10]") {
    assertEquals(tree.fetchElement(Vertex(10, 10)), Some("c"))
  }

  test("should be able to insert an element at a given position.Outside of area at [20, 50]") {
    assertEquals(tree.fetchElement(Vertex(20, 50)), None)
  }

  test("should be able to remove an element from a tree") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement(gridPoint, "test")

    assertEquals(tree.fetchElement(gridPoint), Some("test"))

    val tree2 = tree.removeElement(gridPoint)

    assertEquals(tree2.fetchElement(gridPoint), None)

  }

  test("toBatch") {

    val actual: Batch[String] = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")
      .toBatch

    val expected: Batch[String] =
      Batch("a", "b", "c")

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toBatch with filter") {

    val actual: Batch[String] = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")
      .toBatch(v => v == "b" || v == "c")

    val expected: Batch[String] =
      Batch("b", "c")

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedBatch") {

    val actual: Batch[(Vertex, String)] = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")
      .toBatchWithPosition

    val expected: Batch[(Vertex, String)] =
      Batch(
        (Vertex(0, 0), "a"),
        (Vertex(0, 1), "b"),
        (Vertex(1, 0), "c")
      )

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedBatch with filter") {

    val actual: Batch[(Vertex, String)] = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")
      .toBatchWithPosition(v => v == "b" || v == "c")

    val expected: Batch[(Vertex, String)] =
      Batch(
        (Vertex(0, 1), "b"),
        (Vertex(1, 0), "c")
      )

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    assert(treeA === treeB)
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(1, 0), "c")
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")

    assert(treeA === treeB)
  }

  test("should be able to check equality.not equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "d")

    assert(treeA !== treeB)
  }

  test("should be able to check equality.not equal 2") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(1, 0), "b")
      .insertElement(Vertex(0, 1), "c")

    assert(treeA !== treeB)
  }

  test("should be able to prune an existing tree to simplify the structure") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement(gridPoint, 999)
      .removeElement(gridPoint)
      .prune

    assertEquals(tree === QuadTree.empty(16, 16), true)

  }

  test("should not prune an already optimal tree") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement(gridPoint, 999)

    assertEquals(tree.prune, tree)

  }

  test("should be able to search for a leaf under a point") {
    val tree = QuadTree
      .empty(2, 2)
      .insertElement(Vertex(0, 0), "a")
      .insertElement(Vertex(0, 1), "b")
      .insertElement(Vertex(1, 0), "c")

    val expected: Option[String] = Some("b")

    val point: Vertex = Vertex(0, 1)

    assertEquals(QuadTree.findClosestTo(point, tree), expected)
  }

  test("should allow a search of squares where the line points are in the same square") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(1, 1), Vertex(1, 1))

    val expected: List[String] =
      List(
        "1,1"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares between two horizontal points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(1.1, 1.5), Vertex(3.5, 1.5))

    val expected: List[String] =
      List(
        "1,1",
        "2,1",
        "3,1"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares between two horizontal points - filtered") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(1.1, 1.5), Vertex(3.5, 1.5), _ != "2,1")

    val expected: List[String] =
      List(
        "1,1",
        // "2,1", // filtered out
        "3,1"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares between two vertical points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(2.1, 0.5), Vertex(2.1, 2.1))

    val expected: List[String] =
      List(
        "2,0",
        "2,1",
        "2,2"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares between two 45 degree points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(0.5, 0.5), Vertex(3.5, 3.5))

    val expected: List[String] =
      List(
        "0,0",
        "1,0",
        "0,1",
        "1,1",
        "2,1",
        "1,2",
        "2,2",
        "3,2",
        "2,3",
        "3,3"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  /*
     0 1 2 3
     _ _ _ _
  0 |_|_|_|_|
  1 |s|_|_|_|
  2 |_|_|_|e|
  3 |_|_|_|_|
   */
  test("should allow a search of squares between two diagonal points") {
    val actual = QuadTree.searchByLine(SampleTree.tree, Vertex(0.5, 1.5), Vertex(3.5, 2.5))

    val expected: List[String] =
      List(
        "0,1",
        "1,1",
        "1,2",
        "2,1",
        "2,2",
        "3,2"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a 1x1 rectangle") {
    val r: BoundingBox = BoundingBox(1, 1, 1, 1)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r)

    val expected: List[String] = List("1,1")

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a 2x2 rectangle") {
    val r: BoundingBox = BoundingBox(0, 1, 2, 2)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r, _ != "1,1")

    val expected: List[String] = List(
      "0,1",
      // "1,1", // filtered out
      "0,2",
      "1,2"
    )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a rectangle the size of the grid") {
    val r: BoundingBox = BoundingBox(0, 0, 4, 4)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r, p => p != "1,3")

    val expected: List[String] =
      List(
        "0,0",
        "0,1",
        "0,2",
        "0,3",
        "1,0",
        "1,1",
        "1,2",
        // "1,3", // filtered out
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
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a rectangle") {
    val r: BoundingBox = BoundingBox(0, 1, 4, 2)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r)

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
    assert(actual.forall(expected.contains))
  }

  test("subdivision") {
    val original = BoundingBox(0, 0, 100, 200)

    val (q1, q2, q3, q4) = QuadTree.QuadBranch.subdivide(original)

    assert(q1 ~== BoundingBox(0, 0, 50, 100))
    assert(q2 ~== BoundingBox(50, 0, 50, 100))
    assert(q3 ~== BoundingBox(0, 100, 50, 100))
    assert(q4 ~== BoundingBox(50, 100, 50, 100))

    val recombined: BoundingBox =
      List(q1, q2, q3, q4)
        .reduce(_.expandToInclude(_))

    assert(recombined ~== original)
  }

  test("subdivision 2") {
    val original =
      BoundingBox(
        23.58955381905776,
        13.407618217557008,
        67.05705540086909,
        84.26157060607267
      )

    val (q1, q2, q3, q4) = QuadTree.QuadBranch.subdivide(original)

    val recombined: BoundingBox =
      List(q1, q2, q3, q4)
        .reduce(_.expandToInclude(_))
    // .foldLeft(z)((acc, next) => acc.expandToInclude(next))

    assert(clue(recombined) ~== clue(original))
  }

  test("Point example") {
    given CanEqual[Option[String], Option[String]] = CanEqual.derived

    val actual =
      QuadTree(
        (Point(9, 2), "a"),
        (Point(0, 0), "b"),
        (Point(10, 10), "c")
      )

    val expected =
      QuadBranch(
        BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
        QuadLeaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Point(0, 0), "b"),
        QuadLeaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Point(9, 2), "a"),
        QuadEmpty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
        QuadLeaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Point(10, 10), "c")
      )

    assertEquals(actual, expected)
    assertEquals(actual.findClosestTo(Vertex(9, 9)), Option("c"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(-1, -1, 11, 4)), Batch("a", "b"))
  }

}

object SampleTree {

  val tree: QuadTree[Vertex, String] = QuadTree
    .empty(4, 4)
    .insertElement(Vertex(0, 0), "0,0")
    .insertElement(Vertex(0, 1), "0,1")
    .insertElement(Vertex(0, 2), "0,2")
    .insertElement(Vertex(0, 3), "0,3")
    .insertElement(Vertex(1, 0), "1,0")
    .insertElement(Vertex(1, 1), "1,1")
    .insertElement(Vertex(1, 2), "1,2")
    .insertElement(Vertex(1, 3), "1,3")
    .insertElement(Vertex(2, 0), "2,0")
    .insertElement(Vertex(2, 1), "2,1")
    .insertElement(Vertex(2, 2), "2,2")
    .insertElement(Vertex(2, 3), "2,3")
    .insertElement(Vertex(3, 0), "3,0")
    .insertElement(Vertex(3, 1), "3,1")
    .insertElement(Vertex(3, 2), "3,2")
    .insertElement(Vertex(3, 3), "3,3")

}
