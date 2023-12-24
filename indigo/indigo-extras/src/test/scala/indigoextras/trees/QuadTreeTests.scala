package indigoextras.trees

import indigo.Rectangle
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex
import indigoextras.trees.QuadTree

class QuadTreeTests extends munit.FunSuite {
  given CanEqual[Option[String], Option[String]] = CanEqual.derived

  test("should be able to fetch an element at a given position") {
    val gridPoint: Vertex = Vertex(5, 1)

    val tree: QuadTree.Branch[Vertex, String] = QuadTree.Branch(
      BoundingBox(0, 0, 8, 8),
      QuadTree.Empty(BoundingBox(0, 0, 4, 4)),
      QuadTree.Branch(
        BoundingBox(4, 0, 4, 4),
        QuadTree.Branch(
          BoundingBox(4, 0, 2, 2),
          QuadTree.Empty(BoundingBox(4, 0, 1, 1)),
          QuadTree.Empty(BoundingBox(5, 0, 1, 1)),
          QuadTree.Empty(BoundingBox(4, 1, 1, 1)),
          QuadTree.Leaf(BoundingBox(5, 1, 1, 1), Vertex(5, 1), "hello")
        ),
        QuadTree.Empty(BoundingBox(6, 0, 2, 2)),
        QuadTree.Empty(BoundingBox(4, 2, 2, 2)),
        QuadTree.Empty(BoundingBox(6, 2, 2, 2))
      ),
      QuadTree.Empty(BoundingBox(0, 4, 4, 4)),
      QuadTree.Empty(BoundingBox(4, 4, 4, 4))
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

    val actual =
      QuadTree(
        (Vertex(9, 2), "a"),
        (Vertex(0, 0), "b"),
        (Vertex(10, 10), "c")
      )

    val expected =
      QuadTree.Branch(
        BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
        QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Vertex(0, 0), "b"),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Vertex(9, 2), "a"),
        QuadTree.Empty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Vertex(10, 10), "c")
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

  // TODO: Bring back when we have remove by search functions.
  // test("should be able to prune an existing tree to simplify the structure") {

  //   val gridPoint = Vertex(9, 2)

  //   val tree = QuadTree
  //     .empty(16, 16)
  //     .insertElement(gridPoint, 999)
  //     .removeElement(gridPoint)
  //     .prune

  //   assertEquals(tree === QuadTree.empty(16, 16), true)

  // }

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

    val (q1, q2, q3, q4) = QuadTree.Branch.subdivide(original)

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

    val (q1, q2, q3, q4) = QuadTree.Branch.subdivide(original)

    val recombined: BoundingBox =
      List(q1, q2, q3, q4)
        .reduce(_.expandToInclude(_))

    assert(clue(recombined) ~== clue(original))
  }

  test("Vertex example") {

    val actual =
      QuadTree(
        (Vertex(9, 2), "a"),
        (Vertex(0, 0), "b"),
        (Vertex(10, 10), "c")
      )

    val expected =
      QuadTree.Branch(
        BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
        QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Vertex(0, 0), "b"),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Vertex(9, 2), "a"),
        QuadTree.Empty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Vertex(10, 10), "c")
      )

    assertEquals(actual, expected)
    assertEquals(actual.findClosestTo(Vertex(9.5, 9.5)), Option("c"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(-1, -1, 11, 4)), Batch("a", "b"))
  }

  test("Point example") {

    val actual =
      QuadTree(
        (Point(9, 2), "a"),
        (Point(0, 0), "b"),
        (Point(10, 10), "c")
      )

    val expected =
      QuadTree.Branch(
        BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
        QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Point(0, 0), "b"),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Point(9, 2), "a"),
        QuadTree.Empty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
        QuadTree.Leaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Point(10, 10), "c")
      )

    assertEquals(actual, expected)
    assertEquals(actual.findClosestTo(Vertex(9, 9)), Option("c"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(-1, -1, 11, 4)), Batch("a", "b"))
  }

  test("BoundingBox example (one box)") {

    val actual =
      QuadTree
        .empty(5, 5)
        .insertElements(
          (BoundingBox(0.5, 0.5, 1, 1), "a")
        )

    val expected =
      QuadTree.Leaf(BoundingBox(0, 0, 5, 5), BoundingBox(0.5, 0.5, 1, 1), "a")

    assertEquals(actual, expected)
    assertEquals(actual.findClosestTo(Vertex(1, 3)), Option("a"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)), Batch("a"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)), Batch())
  }

  test("BoundingBox example (two boxes)") {

    val actual =
      QuadTree
        .empty(5, 5)
        .insertElements(
          (BoundingBox(0.5, 0.5, 1, 1), "a"),
          (BoundingBox(2, 0, 2, 4), "b")
        )

    val expected =
      QuadTree.Branch(
        BoundingBox(0, 0, 5, 5),
        QuadTree.Branch(
          BoundingBox(Vertex(0, 0), Vertex(2.5, 2.5)),
          QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(1.25, 1.25)), BoundingBox(0.5, 0.5, 1, 1), "a"),
          QuadTree.Branch(
            BoundingBox(Vertex(1.25, 0), Vertex(1.25, 1.25)),
            QuadTree.Leaf(BoundingBox(Vertex(1.25, 0), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
            QuadTree
              .Leaf(BoundingBox(Vertex(1.875, 0), Vertex(0.625, 0.625)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
            QuadTree.Leaf(BoundingBox(Vertex(1.25, 0.625), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
            QuadTree.Leaf(
              BoundingBox(Vertex(1.875, 0.625), Vertex(0.625, 0.625)),
              BoundingBox(Vertex(2, 0), Vertex(2, 4)),
              "b"
            )
          ),
          QuadTree.Leaf(BoundingBox(Vertex(0, 1.25), Vertex(1.25, 1.25)), BoundingBox(0.5, 0.5, 1, 1), "a"),
          QuadTree.Branch(
            BoundingBox(Vertex(1.25, 1.25), Vertex(1.25, 1.25)),
            QuadTree.Leaf(BoundingBox(Vertex(1.25, 1.25), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
            QuadTree.Leaf(
              BoundingBox(Vertex(1.875, 1.25), Vertex(0.625, 0.625)),
              BoundingBox(Vertex(2, 0), Vertex(2, 4)),
              "b"
            ),
            QuadTree.Empty(BoundingBox(Vertex(1.25, 1.875), Vertex(0.625, 0.625))),
            QuadTree.Leaf(
              BoundingBox(Vertex(1.875, 1.875), Vertex(0.625, 0.625)),
              BoundingBox(Vertex(2, 0), Vertex(2, 4)),
              "b"
            )
          )
        ),
        QuadTree.Leaf(BoundingBox(Vertex(2.5, 0), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
        QuadTree.Leaf(BoundingBox(Vertex(0, 2.5), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
        QuadTree.Leaf(BoundingBox(Vertex(2.5, 2.5), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b")
      )

    assertEquals(actual, expected)
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)), Batch("a", "a"))
    assertEquals(actual.searchByLine(Vertex(0, 0), Vertex(5, 5)), Batch("b", "b", "a", "a", "a", "a", "b", "b", "b"))
    assertEquals(actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).distinct, Batch("b", "a"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)), Batch("b", "b", "b"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).distinct, Batch("b"))
    assertEquals(
      actual.searchByBoundingBox(BoundingBox(0, 0, 5, 4)),
      Batch("b", "b", "a", "b", "a", "b", "a", "a", "a", "b", "b", "b")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(0, 0, 5, 4)).distinct, Batch("b", "a"))
  }

  test("BoundingBox example".only) { // TODO: Remove 'only' when done!

    /*
    Need to implement:
    - Get rid of the 'replace' notion during insert.
    - Add remove by search functions. (?)
    - Max depth: The maximum number of sub-divisions allowed.
    - Min size: The smallest allowed quad size before we give up and group all remaining results here.
    - Multple values: Quad's can hold a Batch of values of the given type.
    - Max values: Quad's can hold a max value before sub-division unless max depth or min size have been hit.
    - Detect duplicates. If a split results in quads that do not change the outcome, stop and group, to prevent infinite depth due to matching values.
    - Move out of extras to Indigo proper
    - SpatialOps instance for Circle + tests
    - SpatialOps instance for BoundingCircle + tests
    - SpatialOps instance for Line + tests
    - SpatialOps instance for LineSegment + tests
    - Check benchmarks.
     */

    fail("Got some work to do here before this will work.")

    // val actual =
    //   QuadTree
    //     .empty(5, 5)
    //     .insertElements(
    //       (BoundingBox(0.5, 0.5, 1, 1), "a"),
    //       (BoundingBox(2, 0, 2, 4), "b"),
    //       (BoundingBox(0.25, 3.25, 4, 0.5), "c")
    //     )

    // println("---")
    // println(actual.prettyPrint)

    // val expected =
    //   QuadTree.Branch(
    //     BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
    //     QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), BoundingBox(2, 0, 2, 4), "b"),
    //     QuadTree.Leaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //     QuadTree.Empty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
    //     QuadTree.Leaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), BoundingBox(0.25, 3.25, 4, 0.5), "c")
    //   )

    // assertEquals(actual, expected)
    // assertEquals(actual.findClosestTo(Vertex(1, 3)), Option("c"))
    // assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)), Batch("a"))
    // assertEquals(actual.searchByLine(Vertex(0, 0), Vertex(5, 5)), Batch("a", "b", "c"))
    // assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)), Batch("b", "c"))
  }

  // test("Rectangle example".only) {
  //

  //   val actual =
  //     QuadTree
  //       .empty(5, 5)
  //       .insertElements(
  //         (Rectangle(1, 1, 1, 1), "a"),
  //         (Rectangle(2, 0, 2, 4), "b"),
  //         (Rectangle(0, 3, 5, 1), "c")
  //       )

  //   val expected =
  //     QuadTree.Branch(
  //       BoundingBox(Vertex(0, 0), Vertex(10.001, 10.001)),
  //       QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(5.0005, 5.0005)), Rectangle(2, 0, 2, 4), "b"),
  //       QuadTree.Leaf(BoundingBox(Vertex(5.0005, 0), Vertex(5.0005, 5.0005)), Rectangle(1, 1, 1, 1), "a"),
  //       QuadTree.Empty(BoundingBox(Vertex(0, 5.0005), Vertex(5.0005, 5.0005))),
  //       QuadTree.Leaf(BoundingBox(Vertex(5.0005, 5.0005), Vertex(5.0005, 5.0005)), Rectangle(0, 3, 5, 1), "c")
  //     )

  //   assertEquals(actual, expected)
  //   assertEquals(actual.findClosestTo(Vertex(1, 3)), Option("c"))
  //   assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)), Batch("a"))
  //   assertEquals(actual.searchByLine(Vertex(0, 0), Vertex(5, 5)), Batch("a", "b", "c"))
  //   assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)), Batch("b", "c"))
  // }

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
