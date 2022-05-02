package indigo.shared.collections

import scalajs.js

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class ChainTests extends munit.FunSuite {

  test("compact") {
    val actual =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    val expected =
      Chain.Wrapped(js.Array(1, 2, 3, 4, 5, 6))

    assertEquals(actual.compact.toList, expected.toList)
  }

  test("head") {
    assert(Chain(1, 2, 3).head == 1)
  }

  test("headOption") {
    assert(Chain(1, 2, 3).headOption == Some(1))
    assert(Chain.Empty.headOption == None)
  }

  test("toString") {
    val a =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    assertEquals(a.toString, "Chain(1, 2, 3, 4, 5, 6)")
  }

  test("equals") {
    assert(Chain.Singleton(1) != Chain.Empty)
    assert(Chain.Singleton(1) == Chain.Wrapped(js.Array(1)))
    assert(Chain.Singleton(2) != Chain.Wrapped(js.Array(1, 2)))
    assert(Chain.Wrapped(js.Array(1, 2)) != Chain.Wrapped(js.Array(2, 1)))
    assert(Chain.Empty == Chain.Empty)
    assert(Chain.Combine(Chain.Empty, Chain.Empty) == Chain.Empty)
    assert(Chain.Combine(Chain.Singleton(1), Chain.Empty) == Chain(1))

    val a: Chain[Int] =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    val b: Chain[Int] =
      Chain(1, 2, 3, 4, 5, 6)

    assert(a == b)
  }

  test("size") {
    val actual =
      Chain
        .Combine(
          Chain.Singleton(1),
          Chain.Combine(
            Chain.Combine(
              Chain(2),
              Chain(3)
            ),
            Chain(4, 5, 6)
          )
        )
        .size

    assert(actual == 6)
  }

  test("toList - empty") {
    assertEquals(Chain.Empty.toList, Nil)
  }

  test("toList - singleton") {
    assertEquals(Chain(1).toList, List(1))
  }

  test("toList - wrapped") {
    assertEquals(Chain(js.Array(1, 2, 3)).toList, List(1, 2, 3))
    assertEquals(Chain(1, 2, 3).toList, List(1, 2, 3))
  }

  test("toList - combine") {
    assertEquals(Chain(Chain(1), Chain(2)).toList, List(1, 2))
  }

  test("toList - nested") {
    val actual =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    assertEquals(actual.toList, List(1, 2, 3, 4, 5, 6))
  }

  test("toArray - nested") {
    val actual =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    assert(actual.toArray.sameElements(Array(1, 2, 3, 4, 5, 6)))
  }

  test("combineAll") {
    assertEquals(Chain.combineAll(Chain(1), Chain(2)).toList, Chain(1, 2).toList)
  }

  test("concat") {
    assertEquals((Chain(1, 2, 3) ++ Chain(4, 5, 6)).toList, List(1, 2, 3, 4, 5, 6))
  }

  test("monoid append") {
    assertEquals((Chain(1) |+| Chain(2, 3)), Chain(1, 2, 3))
  }

  test("cons") {
    assertEquals((1 :: Chain(2, 3)).toList, List(1, 2, 3))
  }

  test("prepend") {
    assertEquals((1 +: Chain(2, 3)).toList, List(1, 2, 3))
  }

  test("append") {
    assertEquals((Chain(2, 3) :+ 1).toList, List(2, 3, 1))
  }

  test("map") {
    val actual =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    assertEquals(
      actual.map(_ * 10).toList,
      List(10, 20, 30, 40, 50, 60)
    )
  }

  test("foreach") {
    val actual =
      Chain.Combine(
        Chain.Singleton(1),
        Chain.Combine(
          Chain.Combine(
            Chain(2),
            Chain(3)
          ),
          Chain(4, 5, 6)
        )
      )

    var res = 0

    actual.foreach(i => res = res + i)

    assert(res == 21)
  }

  test("isEmpty") {
    assert(Chain.Empty.isEmpty)
    assert(!Chain.Singleton(1).isEmpty)
    assert(!Chain.Combine(Chain.Singleton(1), Chain.Singleton(2)).isEmpty)
    assert(!Chain.Wrapped(js.Array(1, 2, 3)).isEmpty)
  }

}
