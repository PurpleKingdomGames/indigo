package indigoextras.trees

import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import indigoextras.trees.QuadTree.QuadBranch
import indigoextras.trees.QuadTree.QuadEmpty
import indigoextras.trees.QuadTree.QuadLeaf

class QuadTreeTests extends munit.FunSuite {

  test("should be able to fetch an element at a given position") {
    val gridPoint: Vertex = Vertex(5, 1)

    val tree: QuadBranch[String] = QuadBranch(
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

    assertEquals(tree.fetchElementAt(gridPoint), Some("hello"))

  }

  val tree = QuadTree
    .empty(16, 16)
    .insertElement("a", Vertex(9, 2))
    .insertElement("b", Vertex(0, 0))
    .insertElement("c", Vertex(10, 10))
    .insertElement("d", Vertex(20, 50))

  test("should be able insert multiple items") {
    given CanEqual[Option[String], Option[String]] = CanEqual.derived

    val actual =
      QuadTree(
        ("a", Vertex(9, 2)),
        ("b", Vertex(0, 0)),
        ("c", Vertex(10, 10))
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
        clue(tree.fetchElementAt(clue(v))) == actual.fetchElementAt(v) &&
        tree.fetchElementAt(v) == expected.fetchElementAt(v)
      }
    )
  }

  test("should be able to insert an element at a given position.[9, 2]") {
    assertEquals(tree.fetchElementAt(Vertex(9, 2)), Some("a"))
  }

  test("should be able to insert an element at a given position.Should be missing at [1, 2]") {
    assertEquals(tree.fetchElementAt(Vertex(1, 2)), None)
  }

  test("should be able to insert an element at a given position.[0, 0]") {
    assertEquals(tree.fetchElementAt(Vertex(0, 0)), Some("b"))
  }

  test("should be able to insert an element at a given position.[10, 10]") {
    assertEquals(tree.fetchElementAt(Vertex(10, 10)), Some("c"))
  }

  test("should be able to insert an element at a given position.Outside of area at [20, 50]") {
    assertEquals(tree.fetchElementAt(Vertex(20, 50)), None)
  }

  test("should be able to remove an element from a tree") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement("test", gridPoint)

    assertEquals(tree.fetchElementAt(gridPoint), Some("test"))

    val tree2 = tree.removeElement(gridPoint)

    assertEquals(tree2.fetchElementAt(gridPoint), None)

  }

  test("toList") {

    val actual: List[String] = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))
      .toList

    val expected: List[String] =
      List("a", "b", "c")

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedList") {

    val actual: List[(Vertex, String)] = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))
      .toListWithPosition

    val expected: List[(Vertex, String)] =
      List(
        (Vertex(0, 0), "a"),
        (Vertex(0, 1), "b"),
        (Vertex(1, 0), "c")
      )

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    assert(treeA === treeB)
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement("c", Vertex(1, 0))
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))

    assert(treeA === treeB)
  }

  test("should be able to check equality.not equal") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("d", Vertex(1, 0))

    assert(treeA !== treeB)
  }

  test("should be able to check equality.not equal 2") {

    val treeA = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    val treeB = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(1, 0))
      .insertElement("c", Vertex(0, 1))

    assert(treeA !== treeB)
  }

  test("should be able to prune an existing tree to simplify the structure") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement(999, gridPoint)
      .removeElement(gridPoint)
      .prune

    assertEquals(tree === QuadTree.empty(16, 16), true)

  }

  test("should not prune an already optimal tree") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty(16, 16)
      .insertElement(999, gridPoint)

    assertEquals(tree.prune, tree)

  }

  test("should be able to search for a leaf under a point") {
    val tree = QuadTree
      .empty(2, 2)
      .insertElement("a", Vertex(0, 0))
      .insertElement("b", Vertex(0, 1))
      .insertElement("c", Vertex(1, 0))

    val expected: Option[String] = Some("b")

    val point: Vertex = Vertex(0, 1)

    assertEquals(QuadTree.findClosestTo(tree, point), expected)
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

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r)

    val expected: List[String] = List(
      "0,1",
      "1,1",
      "0,2",
      "1,2"
    )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a rectangle the size of the grid") {
    val r: BoundingBox = BoundingBox(0, 0, 4, 4)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r)

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

}

object SampleTree {

  val tree: QuadTree[String] = QuadTree
    .empty(4, 4)
    .insertElement("0,0", Vertex(0, 0))
    .insertElement("0,1", Vertex(0, 1))
    .insertElement("0,2", Vertex(0, 2))
    .insertElement("0,3", Vertex(0, 3))
    .insertElement("1,0", Vertex(1, 0))
    .insertElement("1,1", Vertex(1, 1))
    .insertElement("1,2", Vertex(1, 2))
    .insertElement("1,3", Vertex(1, 3))
    .insertElement("2,0", Vertex(2, 0))
    .insertElement("2,1", Vertex(2, 1))
    .insertElement("2,2", Vertex(2, 2))
    .insertElement("2,3", Vertex(2, 3))
    .insertElement("3,0", Vertex(3, 0))
    .insertElement("3,1", Vertex(3, 1))
    .insertElement("3,2", Vertex(3, 2))
    .insertElement("3,3", Vertex(3, 3))

}
