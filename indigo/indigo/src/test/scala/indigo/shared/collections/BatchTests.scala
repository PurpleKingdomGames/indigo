package indigo.shared.collections

import scalajs.js

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class BatchTests extends munit.FunSuite {

  test("apply") {
    val batch =
      Batch.Combine(
        Batch.Singleton(10),
        Batch.Combine(
          Batch.Combine(
            Batch(20),
            Batch(30)
          ),
          Batch(40, 50, 60)
        )
      )

    assertEquals(batch(0), 10)
    assertEquals(batch(1), 20)
    assertEquals(batch(2), 30)
    assertEquals(batch(3), 40)
    assertEquals(batch(4), 50)
    assertEquals(batch(5), 60)
  }

  test("compact") {
    val actual =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    val expected =
      Batch.Wrapped(js.Array(1, 2, 3, 4, 5, 6))

    assertEquals(actual.compact.toList, expected.toList)
  }

  test("head") {
    assert(Batch(1, 2, 3).head == 1)
  }

  test("headOption") {
    assert(Batch(1, 2, 3).headOption == Some(1))
    assert(Batch.Empty.headOption == None)
  }

  test("toString") {
    val a =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    assertEquals(a.toString, "Batch(1, 2, 3, 4, 5, 6)")
  }

  test("equals") {
    assert(Batch.Singleton(1) != Batch.Empty)
    assert(Batch.Singleton(1) == Batch.Wrapped(js.Array(1)))
    assert(Batch.Singleton(2) != Batch.Wrapped(js.Array(1, 2)))
    assert(Batch.Wrapped(js.Array(1, 2)) != Batch.Wrapped(js.Array(2, 1)))
    assert(Batch.Empty == Batch.Empty)
    assert(Batch.Combine(Batch.Empty, Batch.Empty) == Batch.Empty)
    assert(Batch.Combine(Batch.Singleton(1), Batch.Empty) == Batch(1))

    val a: Batch[Int] =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    val b: Batch[Int] =
      Batch(1, 2, 3, 4, 5, 6)

    assert(a == b)
  }

  test("size") {
    val actual =
      Batch
        .Combine(
          Batch.Singleton(1),
          Batch.Combine(
            Batch.Combine(
              Batch(2),
              Batch(3)
            ),
            Batch(4, 5, 6)
          )
        )
        .size

    assert(actual == 6)
  }

  test("toList - empty") {
    assertEquals(Batch.Empty.toList, Nil)
  }

  test("toList - singleton") {
    assertEquals(Batch(1).toList, List(1))
  }

  test("toList - wrapped") {
    assertEquals(Batch(js.Array(1, 2, 3)).toList, List(1, 2, 3))
    assertEquals(Batch(1, 2, 3).toList, List(1, 2, 3))
  }

  test("toList - combine") {
    assertEquals(Batch(Batch(1), Batch(2)).toList, List(1, 2))
  }

  test("toList - nested") {
    val actual =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    assertEquals(actual.toList, List(1, 2, 3, 4, 5, 6))
  }

  test("toArray - nested") {
    val actual =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    assert(actual.toArray.sameElements(Array(1, 2, 3, 4, 5, 6)))
  }

  test("combineAll") {
    assertEquals(Batch.combineAll(Batch(1), Batch(2)).toList, Batch(1, 2).toList)
  }

  test("concat") {
    assertEquals((Batch(1, 2, 3) ++ Batch(4, 5, 6)).toList, List(1, 2, 3, 4, 5, 6))
  }

  test("monoid append") {
    assertEquals((Batch(1) |+| Batch(2, 3)), Batch(1, 2, 3))
  }

  test("cons") {
    assertEquals((1 :: Batch(2, 3)).toList, List(1, 2, 3))
  }

  test("prepend") {
    assertEquals((1 +: Batch(2, 3)).toList, List(1, 2, 3))
  }

  test("append") {
    assertEquals((Batch(2, 3) :+ 1).toList, List(2, 3, 1))
  }

  test("map") {
    val actual =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    assertEquals(
      actual.map(_ * 10).toList,
      List(10, 20, 30, 40, 50, 60)
    )
  }

  test("foreach") {
    val actual =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    var res = 0

    actual.foreach(i => res = res + i)

    assert(res == 21)
  }

  test("isEmpty") {
    assert(Batch.Empty.isEmpty)
    assert(!Batch.Singleton(1).isEmpty)
    assert(!Batch.Combine(Batch.Singleton(1), Batch.Singleton(2)).isEmpty)
    assert(!Batch.Wrapped(js.Array(1, 2, 3)).isEmpty)
  }

  test("splitBatch") {
    val batch =
      Batch.Combine(
        Batch.Singleton(1),
        Batch.Combine(
          Batch.Combine(
            Batch(2),
            Batch(3)
          ),
          Batch(4, 5, 6)
        )
      )

    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
    assertEquals(Batch.splitBatch(Batch.splitBatch(batch)._2)._1, Batch(2))
    assertEquals(Batch.splitBatch(Batch.splitBatch(Batch.splitBatch(batch)._2)._2)._1, Batch(3))
    assertEquals(Batch.splitBatch(Batch.splitBatch(Batch.splitBatch(Batch.splitBatch(batch)._2)._2)._2)._1, Batch(4, 5, 6))
  }

  test("splitBatch - empty left") {
    val batch =
      Batch.Combine(
        Batch.empty,
        Batch(1)
      )

    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
  }

  test("splitBatch - empty right") {
    val batch =
      Batch.Combine(
        Batch(1),
        Batch.empty
      )

    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
  }

  test("splitBatch - combine of combine") {
    val batch =
      Batch.Combine(
        Batch.Combine(
          Batch(1),
          Batch.empty
        ),
        Batch(2)
      )

    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
    assertEquals(Batch.splitBatch(batch)._2, Batch(2))
  }

  test("splitBatch - combine of combine (first empty)") {
    val batch =
      Batch.Combine(
        Batch.Combine(
          Batch.empty,
          Batch(1)
        ),
        Batch(2)
      )

    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
    assertEquals(Batch.splitBatch(batch)._2, Batch(2))
  }

  test("splitBatch - combine of combine of combine") {
    val batch =
      Batch.Combine(
        Batch.Combine(
          Batch.Combine(
            Batch(1),
            Batch.empty
          ),
          Batch.empty
        ),
        Batch(2)
      )


    assertEquals(Batch.splitBatch(batch)._1, Batch(1))
    assertEquals(Batch.splitBatch(batch)._2, Batch(2))
  }

  test("hasNextBatch") {
    assert(!Batch.hasNextBatch(Batch.Empty))
    assert(!Batch.hasNextBatch(Batch(1)))
    assert(!Batch.hasNextBatch(Batch(1,2,3)))
    assert(!Batch.hasNextBatch(Batch.Combine(Batch(1), Batch.Empty)))
    assert(Batch.hasNextBatch(Batch.Combine(Batch(1), Batch(2))))
  }

}
