package indigo.shared.trees

import indigo.BoundingCircle
import indigo.Circle
import indigo.LineSegment
import indigo.Rectangle
import indigo.shared.collections.Batch
import indigo.shared.datatypes.Point
import indigo.shared.geometry.BoundingBox
import indigo.shared.geometry.Vertex

class QuadTreeTests extends munit.FunSuite {
  given CanEqual[Option[String], Option[String]] = CanEqual.derived

  given QuadTree.InsertOptions = QuadTree.options(1, 1, 16)

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
  }

  test("toBatch") {

    val actual: Batch[String] =
      QuadTree
        .empty[Vertex, String](2, 2)
        .insert(Vertex(0, 0), "a")
        .insert(Vertex(0, 1), "b")
        .insert(Vertex(1, 0), "c")
        .toBatch
        .map(_.value)

    val expected: Batch[String] =
      Batch("a", "b", "c")

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedBatch") {

    val actual: Batch[QuadTreeValue[Vertex, String]] =
      QuadTree
        .empty[Vertex, String](2, 2)
        .insert(Vertex(0, 0), "a")
        .insert(Vertex(0, 1), "b")
        .insert(Vertex(1, 0), "c")
        .toBatch

    val expected: Batch[QuadTreeValue[Vertex, String]] =
      Batch(
        QuadTreeValue(Vertex(0, 0), "a"),
        QuadTreeValue(Vertex(0, 1), "b"),
        QuadTreeValue(Vertex(1, 0), "c")
      )

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    assert(treeA === treeB)
  }

  test("should be able to check equality.equal") {

    val treeA = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(1, 0), "c")
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")

    assert(treeA === treeB)
  }

  test("should be able to check equality.not equal") {

    val treeA = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "d")

    assert(treeA !== treeB)
  }

  test("should be able to check equality.not equal 2") {

    val treeA = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    val treeB = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(1, 0), "b")
      .insert(Vertex(0, 1), "c")

    assert(treeA !== treeB)
  }

  test("should be able to prune an existing tree to simplify the structure") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty[Vertex, Int](16, 16)
      .insert(gridPoint, 999)
      .removeClosestTo(gridPoint)
      .prune

    assert(tree === QuadTree.empty[Vertex, Int](16, 16))
  }

  test("should not prune an already optimal tree") {

    val gridPoint = Vertex(9, 2)

    val tree = QuadTree
      .empty[Vertex, Int](16, 16)
      .insert(gridPoint, 999)

    assertEquals(tree.prune, tree)

  }

  test("should be able to search for a leaf under a point") {
    val tree = QuadTree
      .empty[Vertex, String](2, 2)
      .insert(Vertex(0, 0), "a")
      .insert(Vertex(0, 1), "b")
      .insert(Vertex(1, 0), "c")

    val expected: Option[String] = Some("b")

    val point: Vertex = Vertex(0, 1)

    assertEquals(QuadTree.findClosestTo(tree, point).map(_.value), expected)
  }

  test("should allow a search under a vertex") {
    val tree =
      QuadTree
        .empty(4, 4)
        .insert(
          Vertex(1) -> "a",
          Vertex(2) -> "b"
        )

    assertEquals(tree.searchAt(Vertex(1, 1)).map(_.value), Batch("a"))
    assertEquals(tree.searchAt(Vertex(2, 1)).map(_.value), Batch())
    assertEquals(tree.searchAt(Vertex(1, 2)).map(_.value), Batch())
    assertEquals(tree.searchAt(Vertex(2, 2)).map(_.value), Batch("b"))
  }

  test("should allow a remove under a vertex") {
    val tree =
      QuadTree
        .empty(4, 4)
        .insert(
          Vertex(1) -> "a",
          Vertex(2) -> "b"
        )

    assertEquals(tree.removeAt(Vertex(1, 1)).toBatch.map(_.value), Batch("b"))
  }

  test("should allow a search of squares where the line points are in the same square") {
    val actual = SampleTree.tree.searchByLine(Vertex(1, 1), Vertex(1, 1))

    val expected: List[String] =
      List(
        "1,1"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.map(_.value).forall(expected.contains))
  }

  test("should allow a search of squares between two horizontal points") {
    val actual = SampleTree.tree.searchByLine(Vertex(1.1, 1.5), Vertex(3.5, 1.5))

    val expected: List[String] =
      List(
        "1,1",
        "2,1",
        "3,1"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.map(_.value).forall(expected.contains))
  }

  test("should allow a search of squares between two vertical points") {
    val actual = SampleTree.tree.searchByLine(Vertex(2.1, 0.5), Vertex(2.1, 2.1))

    val expected: List[String] =
      List(
        "2,0",
        "2,1",
        "2,2"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.map(_.value).forall(expected.contains))
  }

  test("should allow a search of squares between two 45 degree points") {
    val actual = SampleTree.tree.searchByLine(Vertex(0.5, 0.5), Vertex(3.5, 3.5))

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
    assert(actual.map(_.value).forall(expected.contains))
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
    val actual = SampleTree.tree.searchByLine(Vertex(0.5, 1.5), Vertex(3.5, 2.5))

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
    assert(actual.map(_.value).forall(expected.contains))
  }

  test("should allow a search of squares intersecting with a 1x1 rectangle") {
    val r: BoundingBox = BoundingBox(1, 1, 1, 1)

    val actual = QuadTree.searchByBoundingBox(SampleTree.tree, r)

    val expected: List[String] = List("1,1")

    assertEquals(actual.length, expected.length)
    assert(actual.map(_.value).forall(expected.contains))
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
    assert(actual.map(_.value).forall(expected.contains))
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
    assert(actual.map(_.value).forall(expected.contains))
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
    assert(actual.map(_.value).forall(expected.contains))
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

    // Searching
    assertEquals(actual.findClosestTo(Vertex(9.5, 9.5)).map(_.value), Option("c"))
    assertEquals(actual.searchByLine(Vertex.zero, Vertex(1, 1)).map(_.value), Batch("b"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(-1, -1, 11, 4)).map(_.value), Batch("a", "b"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("a"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("a"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(10, 10)).toBatch.map(_.value), Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(-1, -1, 11, 4)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterAt(Vertex(0, 0), _.value == "b").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex(0, 0), Vertex(5, 5), _.value == "c").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(0, 0, 5, 2), _.value == "a").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
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

    // Searching
    assertEquals(actual, expected)
    assertEquals(actual.findClosestTo(Vertex(9, 9)).map(_.value), Option("c"))
    assertEquals(actual.searchByLine(Vertex.zero, Vertex(1, 1)).map(_.value), Batch("b"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(-1, -1, 11, 4)).map(_.value), Batch("a", "b"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("a"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("a"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(10, 10)).toBatch.map(_.value), Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(-1, -1, 11, 4)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterAt(Vertex(0, 0), _.value == "b").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex(0, 0), Vertex(5, 5), _.value == "c").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(0, 0, 5, 2), _.value == "a").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
  }

  test("BoundingBox example (one box)") {

    val actual =
      QuadTree
        .empty[BoundingBox, String](5, 5)
        .insert(
          (BoundingBox(0.5, 0.5, 1, 1), "a")
        )

    val expected =
      QuadTree.Leaf(BoundingBox(0, 0, 5, 5), BoundingBox(0.5, 0.5, 1, 1), "a")

    assertEquals(actual, expected)

    // Searching
    assertEquals(actual.findClosestTo(Vertex(1, 3)).map(_.value), Option("a"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)), Batch())

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), None)
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), None)
    assertEquals(actual.removeByLine(Vertex(0, 2), Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 5, 2)).findClosestTo(Vertex.zero).map(_.value),
      None
    )
  }

  test("BoundingBox example (two boxes)") {

    val actual =
      QuadTree
        .empty[BoundingBox, String](5, 5)
        .insert(
          (BoundingBox(0.5, 0.5, 1, 1), "a"),
          (BoundingBox(2, 0, 2, 4), "b")
        )

    // val expected =
    //   QuadTree.Branch(
    //     BoundingBox(0, 0, 5, 5),
    //     QuadTree.Branch(
    //       BoundingBox(Vertex(0, 0), Vertex(2.5, 2.5)),
    //       QuadTree.Leaf(BoundingBox(Vertex(0, 0), Vertex(1.25, 1.25)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //       QuadTree.Branch(
    //         BoundingBox(Vertex(1.25, 0), Vertex(1.25, 1.25)),
    //         QuadTree.Leaf(BoundingBox(Vertex(1.25, 0), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //         QuadTree
    //           .Leaf(BoundingBox(Vertex(1.875, 0), Vertex(0.625, 0.625)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
    //         QuadTree.Leaf(BoundingBox(Vertex(1.25, 0.625), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //         QuadTree.Leaf(
    //           BoundingBox(Vertex(1.875, 0.625), Vertex(0.625, 0.625)),
    //           BoundingBox(Vertex(2, 0), Vertex(2, 4)),
    //           "b"
    //         )
    //       ),
    //       QuadTree.Leaf(BoundingBox(Vertex(0, 1.25), Vertex(1.25, 1.25)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //       QuadTree.Branch(
    //         BoundingBox(Vertex(1.25, 1.25), Vertex(1.25, 1.25)),
    //         QuadTree.Leaf(BoundingBox(Vertex(1.25, 1.25), Vertex(0.625, 0.625)), BoundingBox(0.5, 0.5, 1, 1), "a"),
    //         QuadTree.Leaf(
    //           BoundingBox(Vertex(1.875, 1.25), Vertex(0.625, 0.625)),
    //           BoundingBox(Vertex(2, 0), Vertex(2, 4)),
    //           "b"
    //         ),
    //         QuadTree.Empty(BoundingBox(Vertex(1.25, 1.875), Vertex(0.625, 0.625))),
    //         QuadTree.Leaf(
    //           BoundingBox(Vertex(1.875, 1.875), Vertex(0.625, 0.625)),
    //           BoundingBox(Vertex(2, 0), Vertex(2, 4)),
    //           "b"
    //         )
    //       )
    //     ),
    //     QuadTree.Leaf(BoundingBox(Vertex(2.5, 0), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
    //     QuadTree.Leaf(BoundingBox(Vertex(0, 2.5), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b"),
    //     QuadTree.Leaf(BoundingBox(Vertex(2.5, 2.5), Vertex(2.5, 2.5)), BoundingBox(Vertex(2, 0), Vertex(2, 4)), "b")
    //   )

    // Searching
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).map(_.value),
      Batch("a", "b", "a", "a", "b", "a", "b", "b", "b")
    )
    assertEquals(actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).distinct.map(_.value), Batch("a", "b"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 1.5, 1, 1)).map(_.value), Batch("b"))
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).distinct.map(_.value), Batch("b"))
    assertEquals(
      actual.searchByBoundingBox(BoundingBox(0, 0, 5, 4)).map(_.value),
      Batch("a", "b", "a", "a", "b", "a", "b", "b", "b")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(0, 0, 5, 4)).distinct.map(_.value), Batch("a", "b"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex(0, 2), Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 5, 2)).findClosestTo(Vertex.zero).map(_.value),
      None
    )
  }

  test("BoundingBox example") {

    val actual =
      QuadTree
        .empty[BoundingBox, String](5, 5)
        .insert(
          (BoundingBox(0.5, 0.5, 1, 1), "a"),
          (BoundingBox(2, 0, 2, 4), "b"),
          (BoundingBox(0.25, 3.25, 4, 0.5), "c")
        )

    // Searching
    assertEquals(actual.findClosestTo(Vertex(1, 3)).map(_.value), Option("c"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).map(_.value),
      Batch("b", "b", "b", "c", "b", "c", "b", "c", "a", "b", "a", "a", "b", "a", "b")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).map(_.value), Batch("b", "c"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex(0, 2), Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 5, 2)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex(0, 0), Vertex(5, 5), _.value != "a").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(0, 0, 5, 2), _.value != "a").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
  }

  test("BoundingCircle example") {

    val actual =
      QuadTree
        .empty[BoundingCircle, String](5, 5)
        .insert(
          (BoundingCircle(1, 1, 0.5), "a"),
          (BoundingCircle(3.5, 2.5, 1.5), "b"),
          (BoundingCircle(2, 4, 1), "c")
        )

    // Searching
    assertEquals(actual.findClosestTo(Vertex(2, 0.5)).map(_.value), Option("a"))
    assertEquals(actual.findClosestTo(Vertex(2, 2)).map(_.value), Option("b"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(1, 0), Vertex(3.5, 5)).map(_.value),
      Batch("b", "c", "b", "c", "b", "c", "a", "b", "a", "a")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).map(_.value), Batch("b", "c"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex(0, 2), Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 5, 2)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(5, 5), _.value == "b").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(-1, -1, 6, 3), _.value == "b").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
  }

  test("Rectangle example") {

    val actual =
      QuadTree
        .empty[Rectangle, String](5, 5)
        .insert(
          (Rectangle(1, 1, 1, 1), "a"),
          (Rectangle(2, 0, 2, 4), "b"),
          (Rectangle(0, 3, 5, 1), "c")
        )

    // Searching
    assertEquals(actual.findClosestTo(Vertex(1, 3)).map(_.value), Option("c"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).map(_.value),
      Batch("b", "c", "b", "c", "b", "c", "b", "c", "b", "c", "a", "b", "a", "a", "b", "a", "b")
    )
    assertEquals(
      actual.searchByLine(Vertex(0, 0), Vertex(5, 5)).map(_.value).distinct,
      Batch("b", "c", "a")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).map(_.value), Batch("b", "c"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex(0, 2), Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("a"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 5, 2)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex(0, 0), Vertex(5, 5), _.value != "a").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(0, 0, 5, 2), _.value != "a").toBatch.map(_.value).distinct,
      Batch("b", "c")
    )
  }

  test("Circle example") {

    val actual =
      QuadTree
        .empty[Circle, String](5, 5)
        .insert(
          (Circle(1, 1, 1), "a"),
          (Circle(3, 2, 1), "b"),
          (Circle(2, 4, 1), "c")
        )

    // Searching
    assertEquals(actual.findClosestTo(Vertex(2, 0.5)).map(_.value), Option("a"))
    assertEquals(actual.findClosestTo(Vertex(2, 2)).map(_.value), Option("b"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(1, 0), Vertex(3.5, 5)).map(_.value).distinct,
      Batch("c", "b", "a")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(1.5, 2.5, 1, 1)).map(_.value), Batch("b", "c"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(1, 1)).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(5, 5)).toBatch.map(_.value).distinct, Batch("c"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(-1, -1, 6, 3)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filter(_.value != "b").toBatch.map(_.value).distinct,
      Batch("c", "a")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1, 1), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(5, 5), _.value == "b").toBatch.map(_.value).distinct,
      Batch("c", "b")
    )
    assertEquals(
      actual.filterByBoundingBox(BoundingBox(-1, -1, 6, 3), _.value == "b").toBatch.map(_.value).distinct,
      Batch("c", "b")
    )
  }

  test("LineSegment example") {

    val actual =
      QuadTree
        .empty[LineSegment, String](5, 5)
        .insert(
          (LineSegment((0.5, 1.5), (1.5, 0.5)), "a"),
          (LineSegment((2.5, 0.5), (3.5, 3.5)), "b"),
          (LineSegment((3.5, 1.5), (1.5, 3.5)), "c")
        )

    // Searching
    assertEquals(actual.findClosestTo(Vertex(2, 0.5)).map(_.value), Option("a"))
    assertEquals(actual.findClosestTo(Vertex(2, 2)).map(_.value), Option("c"))
    assertEquals(actual.searchByLine(Vertex(0, 2), Vertex(1, 0)).map(_.value), Batch("a", "a"))
    assertEquals(
      actual.searchByLine(Vertex(1, 0), Vertex(3.5, 5)).map(_.value).distinct,
      Batch("b", "c", "a")
    )
    assertEquals(actual.searchByBoundingBox(BoundingBox(2.5, 1.5, 1, 1)).map(_.value), Batch("b", "c"))

    // Removing
    assertEquals(actual.removeClosestTo(Vertex.zero).findClosestTo(Vertex.zero).map(_.value), Option("b"))
    assertEquals(
      actual.removeByLine(Vertex.zero, Vertex(1.5, 1.5)).findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(actual.removeByLine(Vertex.zero, Vertex(3, 3)).toBatch.map(_.value).distinct, Batch("b"))
    assertEquals(
      actual.removeByBoundingBox(BoundingBox(0, 0, 3, 1)).findClosestTo(Vertex.zero).map(_.value),
      Option("c")
    )

    // Filtering
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(1.5, 1.5), _.value != "a").findClosestTo(Vertex.zero).map(_.value),
      Option("b")
    )
    assertEquals(
      actual.filterByLine(Vertex.zero, Vertex(3, 3), _.value == "a").toBatch.map(_.value).distinct,
      Batch("b", "a")
    )
    assertEquals(
      actual
        .filterByBoundingBox(BoundingBox(0, 0, 3, 1), _.value == "b")
        .toBatch
        .map(_.value)
        .distinct,
      Batch("b", "c")
    )
  }

}

object SampleTree {

  given QuadTree.InsertOptions = QuadTree.options(1, 0.1, 16)

  val tree: QuadTree[Vertex, String] = QuadTree
    .empty[Vertex, String](4, 4)
    .insert(Vertex(0, 0), "0,0")
    .insert(Vertex(0, 1), "0,1")
    .insert(Vertex(0, 2), "0,2")
    .insert(Vertex(0, 3), "0,3")
    .insert(Vertex(1, 0), "1,0")
    .insert(Vertex(1, 1), "1,1")
    .insert(Vertex(1, 2), "1,2")
    .insert(Vertex(1, 3), "1,3")
    .insert(Vertex(2, 0), "2,0")
    .insert(Vertex(2, 1), "2,1")
    .insert(Vertex(2, 2), "2,2")
    .insert(Vertex(2, 3), "2,3")
    .insert(Vertex(3, 0), "3,0")
    .insert(Vertex(3, 1), "3,1")
    .insert(Vertex(3, 2), "3,2")
    .insert(Vertex(3, 3), "3,3")

}
