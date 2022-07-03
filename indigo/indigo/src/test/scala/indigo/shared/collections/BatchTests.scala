package indigo.shared.collections

import scalajs.js

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class BatchTests extends munit.FunSuite {

  test("update a value") {
    val actual   = Batch(1, 2, 3, 4, 5).update(2, 10)
    val expected = Batch(1, 2, 10, 4, 5)
    assertEquals(actual, expected)
  }

  test("insert a value") {
    val actual   = Batch(1, 2, 3, 4, 5).insert(2, 10)
    val expected = Batch(1, 2, 10, 3, 4, 5)
    assertEquals(actual, expected)
  }

  test("pattern matching - empty") {
    import Batch.==:
    Batch.empty[Int] match
      case Batch()  => assert(true)
      case i ==: is => assert(i == 1)
      case _        => assert(false)
  }

  test("pattern matching - first") {
    import Batch.==:
    Batch(1, 2, 3) match
      case Batch()  => assert(false)
      case i ==: is => assert(i == 1)
      case _        => assert(false)
  }

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
    assert(Batch.Combine(Batch(1), Batch(2, 3)).head == 1)
    assert((Batch.empty |+| Batch(2, 3)).head == 2)
    assert(Batch(Batch.Empty, Batch(1, 2, 3)).head == Batch.empty)
    assert(Batch.combine(Batch.Empty, Batch(1, 2, 3)).head == 1)
  }

  test("headOption") {
    assert(Batch(1, 2, 3).headOption == Some(1))
    assert(Batch.Empty.headOption == None)
    assert(Batch.Combine(Batch(1), Batch(2, 3)).headOption == Some(1))
    assert((Batch.empty |+| Batch(2, 3)).headOption == Some(2))
    assert(Batch(Batch.Empty, Batch(1, 2, 3)).headOption == Some(Batch.empty))
    assert(Batch.combine(Batch.Empty, Batch(1, 2, 3)).headOption == Some(1))
  }

  test("tail") {
    assert(Batch(1, 2, 3).tail == Batch(2, 3))
  }

  test("tail - empty") {
    assert(Batch.empty.tail == Batch.empty)
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

  test("mkString") {
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

    assertEquals(a.mkString, "123456")
    assertEquals(a.mkString("-"), "1-2-3-4-5-6")
    assertEquals(a.mkString("{", ":", "}"), "{1:2:3:4:5:6}")
  }

  test("mkString - empty") {
    assertEquals(Batch.empty.mkString, "")
  }

  test("exists") {
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

    assert(a.exists(_ == 5))
    assert(!a.exists(_ == 7))
  }

  test("find") {
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

    assertEquals(a.find(_ > 5), Some(6))
    assertEquals(a.find(_ < 1), None)
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
    assertEquals((Batch(1) |+| Batch(2)).toList, List(1, 2))
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

  test("flatMap") {
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
      actual.flatMap(v => Batch(v * 10)).toList,
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

  test("foreachWithIndex") {
    val actual =
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

    var res = Array[(Int, Int)]()

    actual.foreachWithIndex { case (value, index) =>
      res = res ++ Array((index, value))
    }

    val expected =
      List(
        (0, 10),
        (1, 20),
        (2, 30),
        (3, 40),
        (4, 50),
        (5, 60)
      )

    assertEquals(res.toList, expected)
  }

  test("isEmpty") {
    assert(Batch.Empty.isEmpty)
    assert(!Batch.Singleton(1).isEmpty)
    assert(!Batch.Combine(Batch.Singleton(1), Batch.Singleton(2)).isEmpty)
    assert(!Batch.Wrapped(js.Array(1, 2, 3)).isEmpty)
  }

}
