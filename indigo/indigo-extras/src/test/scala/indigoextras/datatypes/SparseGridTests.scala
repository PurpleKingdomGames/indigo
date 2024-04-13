package indigoextras.datatypes

import indigo.*

class SparseGridTests extends munit.FunSuite {

  test("should be able to put and get an element at a given position") {
    val grid =
      SparseGrid(Size(3))
        .put(Point(1, 1), "@")

    val expected =
      Option("@")

    val actual =
      grid.get(Point(1))

    assertEquals(expected, actual)
  }

  test("trying to get at an empty location returns None") {
    val grid =
      SparseGrid(Size(3))
        .put(Point(1, 1), "@")

    val expected: Option[String] =
      None

    val actual =
      grid.get(Point(0))

    assertEquals(expected, actual)
  }

  test("should be able to remove an element at a given position") {
    val grid =
      SparseGrid(Size(3))
        .put(Point(1), "@")
        .remove(Point(1))

    val expected: Option[String] =
      None

    val actual =
      grid.get(Point(1))

    assertEquals(expected, actual)
  }

  test("should be able insert multiple items") {
    val list =
      Batch(
        (Point(8, 2), "@"),
        (Point(0, 0), "!"),
        (Point(9, 9), "?")
      )

    val grid =
      SparseGrid(Size(10))
        .put(list)

    assert(
      Batch(Point(8, 2), Point(0, 0), Point(9, 9)).forall { v =>
        clue(grid.get(clue(v))) == list.find(p => p._1 == v).map(_._2)
      }
    )
  }

  test("continuous list (empty)") {
    val grid =
      SparseGrid[Int](Size(3))

    val actual =
      grid.toBatch

    val expected =
      Batch.fill[Option[Int]](9)(None)

    assertEquals(actual.length, expected.length)
    assertEquals(actual.toList, expected.toList)
  }

  test("continuous list (full)") {
    val coords =
      (0 to 2).flatMap { y =>
        (0 to 2).map { x =>
          Point(x, y)
        }
      }.toList

    val items: Batch[(Point, String)] =
      Batch.fromList(coords.zip(List.fill(8)("!") :+ "@"))

    val grid =
      SparseGrid(Size(3))
        .put(items)

    val actual =
      grid.toBatch.collect { case Some(v) => v }

    val expected =
      List(
        "!",
        "!",
        "!",
        "!",
        "!",
        "!",
        "!",
        "!",
        "@"
      )

    assertEquals(actual.length, expected.length)
    assertEquals(actual.toList, expected)
  }

  test("continuous list (sparse)") {
    val coords =
      List(
        Point(0, 0),
        // Point(0, 1),
        // Point(0, 2),
        Point(1, 0),
        Point(1, 1),
        // Point(1, 2),
        Point(2, 0),
        Point(2, 1),
        Point(2, 2)
      )

    val items: List[String] =
      List(
        "a",
        "b",
        "c",
        "d",
        "e",
        "f"
      )

    val itemsWithCoords: Batch[(Point, String)] =
      Batch.fromList(coords.zip(items))

    val grid =
      SparseGrid(Size(3))
        .put(itemsWithCoords)

    val actual =
      grid.toBatch.collect { case Some(v) => v }

    val expected =
      List(
        "a",
        "b",
        "c",
        "d",
        "e",
        "f"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
    // fail("asd")
  }

  test("continuous list (sparse, with default)") {
    val coords =
      List(
        Point(0, 0),
        // Point(0, 1),
        // Point(0, 2),
        Point(1, 0),
        Point(1, 1),
        // Point(1, 2),
        Point(2, 0),
        Point(2, 1),
        Point(2, 2)
      )

    val items: List[String] =
      List(
        "a",
        "b",
        "c",
        "d",
        "e",
        "f"
      )

    val itemsWithCoords: Batch[(Point, String)] =
      Batch.fromList(coords.zip(items))

    val grid =
      SparseGrid(Size(3))
        .put(itemsWithCoords)

    val actual =
      grid.toBatch("x")

    val expected =
      List(
        "a",
        "x",
        "x",
        "b",
        "c",
        "x",
        "d",
        "e",
        "f"
      )

    assertEquals(actual.length, expected.length)
    assert(actual.forall(expected.contains))
  }

  test("combine") {
    val consoleA =
      SparseGrid(Size(3))
        .put(Point(1, 1), "@")

    val consoleB =
      SparseGrid(Size(3))
        .put(Point(2, 2), "!")

    val combined =
      consoleA `combine` consoleB

    assert(combined.get(Point(1)).get == "@")
    assert(combined.get(Point(2)).get == "!")
  }

  test("toBatch") {
    val consoleA =
      SparseGrid(Size(3))
        .put(Point(1, 1), "@")

    val consoleB =
      SparseGrid(Size(3))
        .put(Point(2, 2), "!")

    val expected =
      List(None, None, None, None, Some("@"), None, None, None, Some("!"))

    val actual =
      (consoleA `combine` consoleB).toBatch

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toBatch - region") {
    val grid =
      SparseGrid(Size(3))
        .fill("!")
        .put(Point(1), "@")

    val expected =
      Batch(
        "@",
        "!",
        "!",
        "!"
      )

    val actual =
      grid.toBatch(Rectangle(1, 1, 2, 2))

    assert(clue(actual.length) == clue(expected.length))
    assert(clue(actual).forall(clue(expected).contains))
  }

  test("toBatch - region") {
    val grid =
      SparseGrid(Size(3))
        .fill("!")
        .put(Point(1), "@")

    val expected =
      Batch(
        "@",
        "!",
        "!",
        "!"
      )

    val actual =
      grid.toBatch(Rectangle(1, 1, 2, 2))

    assert(clue(actual.length) == clue(expected.length))
    assert(clue(actual).forall(clue(expected).contains))
    assert(clue(actual).head == "@")
  }

  test("toBatch - region with default") {
    val grid =
      SparseGrid(Size(3))
        .put(Point(1), "@")

    val expected =
      Batch(
        "@",
        "x",
        "x",
        "x"
      )

    val actual =
      grid.toBatch(Rectangle(1, 1, 2, 2), "x")

    assert(clue(actual.length) == clue(expected.length))
    assert(clue(actual).forall(clue(expected).contains))
    assert(clue(actual).head == "@")
  }

  test("toPositionedBatch") {
    val consoleA =
      SparseGrid(Size(3))
        .put(Point(1, 1), "@")

    val consoleB =
      SparseGrid(Size(3))
        .put(Point(2, 2), "!")

    val expected =
      List((Point(1), "@"), (Point(2), "!"))

    val actual =
      (consoleA `combine` consoleB).toPositionedBatch

    assert(actual.length == expected.length)
    assert(actual.forall(expected.contains))
  }

  test("toPositionedBatch - region") {
    val grid =
      SparseGrid(Size(3))
        .fill("!")
        .put(Point(1), "@")

    val expected =
      Batch(
        Point(1, 1) -> "@",
        Point(2, 1) -> "!",
        Point(1, 2) -> "!",
        Point(2, 2) -> "!"
      )

    val actual =
      grid.toPositionedBatch(Rectangle(1, 1, 2, 2))

    assert(clue(actual.length) == clue(expected.length))
    assert(clue(actual).forall(clue(expected).contains))
    assert(clue(actual).exists(p => p._1 == Point(1) && p._2 == "@"))
  }

  test("placing something in the center works.") {
    val grid =
      SparseGrid(Size(80, 50))
        .put(Point(40, 25), "@")

    val expected =
      Option("@")

    val actual =
      grid.get(Point(40, 25))

    assertEquals(expected, actual)

    val list =
      grid.toPositionedBatch

    assert(list.contains((Point(40, 25), "@")))

    val drawn =
      grid.toBatch

    val foundAt =
      drawn.zipWithIndex.find(p => p._1 == Some("@")).map(_._2)

    assert(drawn.contains(Some("@")))
    assert(drawn.filter(_ == Some("@")).length == 1)
    assert(drawn.length == 80 * 50)
    assert(foundAt.nonEmpty)
    assert(clue(foundAt.get) == 2040)
  }

  test("inset") {
    val gridA =
      SparseGrid(Size(3))
        .put(Point(0), "@")
    val gridB =
      SparseGrid(Size(803))
        .put(Point(0, 0), "!")
        .put(Point(1, 1), "!")
        .put(Point(0, 1), "?")

    val expected =
      Batch(
        Some("@"),
        None,
        None,
        None,
        Some("!"),
        None,
        None,
        Some("?"),
        Some("!")
      )

    val actual =
      gridA.inset(gridB, Point(1)).toBatch

    assertEquals(actual, expected)
  }

  test("modifyAt") {
    val actual =
      SparseGrid(Size(3))
        .fill(".")
        .modifyAt(Point(1))(_.map(_ + "!"))
        .toBatch

    val expected =
      Batch(
        Batch(
          ".",
          ".",
          "."
        ),
        Batch(
          ".",
          ".!",
          "."
        ),
        Batch(
          ".",
          ".",
          "."
        )
      ).flatten

    assert(actual.length == expected.length)
    assert(actual.map(_.get).zip(expected).forall(_ == _))
  }

  test("map") {
    val actual =
      SparseGrid(Size(3))
        .fill(".")
        .map {
          case (pt, _) if pt == Point(0) || pt == Point(1) || pt == Point(2) =>
            Option("?")

          case (_, mt) =>
            mt
        }
        .toBatch

    val expected =
      Batch(
        Batch(
          "?",
          ".",
          "."
        ),
        Batch(
          ".",
          "?",
          "."
        ),
        Batch(
          ".",
          ".",
          "?"
        )
      ).flatten

    assert(actual.length == expected.length)
    assert(actual.map(_.get).zip(expected).forall(_ == _))
  }

  test("mapRectangle") {
    val actual =
      SparseGrid(Size(5))
        .fill("-")
        .mapRectangle(Rectangle(1, 1, 3, 3)) { (_, _) =>
          Option(".")
        }
        .toBatch

    val expected =
      Batch(
        Batch(
          "-",
          "-",
          "-",
          "-",
          "-"
        ),
        Batch(
          "-",
          ".",
          ".",
          ".",
          "-"
        ),
        Batch(
          "-",
          ".",
          ".",
          ".",
          "-"
        ),
        Batch(
          "-",
          ".",
          ".",
          ".",
          "-"
        ),
        Batch(
          "-",
          "-",
          "-",
          "-",
          "-"
        )
      ).flatten

    assert(actual.length == expected.length)
    assert(actual.map(_.get).zip(expected).forall(_ == _))
  }

  test("mapCircle") {
    val actual =
      SparseGrid(Size(5))
        .fill("-")
        .mapCircle(Circle(2, 2, 2)) { (_, _) =>
          Option(".")
        }
        .toBatch

    val expected =
      Batch(
        Batch(
          "-",
          "-",
          ".",
          "-",
          "-"
        ),
        Batch(
          "-",
          ".",
          ".",
          ".",
          "-"
        ),
        Batch(
          ".",
          ".",
          ".",
          ".",
          "."
        ),
        Batch(
          "-",
          ".",
          ".",
          ".",
          "-"
        ),
        Batch(
          "-",
          "-",
          ".",
          "-",
          "-"
        )
      ).flatten

    assert(actual.length == expected.length)
    assert(actual.map(_.get).zip(expected).forall(_ == _))
  }

  test("mapLine") {
    val actual =
      SparseGrid(Size(5))
        .fill("-")
        .mapLine(Point(0), Point(4)) { (_, _) =>
          Option(".")
        }
        .toBatch

    val expected =
      Batch(
        Batch(
          ".",
          "-",
          "-",
          "-",
          "-"
        ),
        Batch(
          "-",
          ".",
          "-",
          "-",
          "-"
        ),
        Batch(
          "-",
          "-",
          ".",
          "-",
          "-"
        ),
        Batch(
          "-",
          "-",
          "-",
          ".",
          "-"
        ),
        Batch(
          "-",
          "-",
          "-",
          "-",
          "."
        )
      ).flatten

    assert(actual.length == expected.length)
    assert(actual.map(_.get).zip(expected).forall(_ == _))
  }

}
