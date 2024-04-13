package indigo.shared.collections

import indigo.shared.Outcome
import indigo.syntax.*

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
class NonEmptyBatchTests extends munit.FunSuite {

  test("pattern matching - first") {
    import NonEmptyBatch.==:
    NonEmptyBatch(1, 2, 3) match
      case i ==: is => assert(i == 1)
      case _        => assert(false)
  }

  test("NonEmptyBatch AsString should be able to show a batch of Ints") {
    val nel = NonEmptyBatch(1, 2, 3)
    assertEquals(nel.toString(), "NonEmptyBatch[1][2, 3]")
  }

  test("NonEmptyBatch usage can be pattern matched") {
    NonEmptyBatch(1, 2, 3) match {
      case NonEmptyBatch(head, tail) =>
        assertEquals(head, 1)
        assertEquals(tail, Batch(2, 3))

      case null =>
        fail("shouldn't have got here")
    }
  }

  test("NonEmptyBatch ops.should have: equality") {
    assertEquals(NonEmptyBatch(1) == NonEmptyBatch(1), true)
  }

  test("NonEmptyBatch ops.should have: point") {
    assertEquals(NonEmptyBatch(1) == NonEmptyBatch(1), true)
  }

  test("NonEmptyBatch ops.should have: map") {
    assertEquals(NonEmptyBatch(1, 2, 3).map(_ * 10) == NonEmptyBatch(10, 20, 30), true)
  }

  test("NonEmptyBatch ops.should have: reverse") {
    assertEquals(NonEmptyBatch(1, 2, 3, 4, 5).reverse == NonEmptyBatch(5, 4, 3, 2, 1), true)
    assertEquals(NonEmptyBatch(1) == NonEmptyBatch(1), true)
  }

  test("NonEmptyBatch ops.should have: combine") {
    assertEquals(NonEmptyBatch(1) ++ NonEmptyBatch(2) == NonEmptyBatch(1, 2), true)
  }

  test("NonEmptyBatch ops.should have: flatten") {
    assertEquals(NonEmptyBatch.flatten(NonEmptyBatch(NonEmptyBatch.point(1))), NonEmptyBatch(1))
  }

  test("NonEmptyBatch ops.should have: flatMap") {
    assertEquals(NonEmptyBatch(1, 2, 3).flatMap(i => NonEmptyBatch(i * 10 + 1)) == NonEmptyBatch(11, 21, 31), true)
  }

  test("NonEmptyBatch ops.should have: foldLeft") {
    assertEquals(NonEmptyBatch("a", "b", "c").foldLeft("")(_ + _), "abc")
  }

  test("NonEmptyBatch ops.should have: reduceLeft") {
    assertEquals(NonEmptyBatch("a", "b", "c").reduce(_ + _), "abc")
  }

  test("NonEmptyBatch ops.should have: append") {
    assertEquals(NonEmptyBatch(1) :+ 2 == NonEmptyBatch(1, 2), true)
  }

  test("NonEmptyBatch ops.should have: cons") {
    assertEquals(1 :: NonEmptyBatch(2) == NonEmptyBatch(1, 2), true)
  }

  given CanEqual[(String, Int), (String, Int)] = CanEqual.derived

  test("NonEmptyBatch ops.should have: zipWithIndex") {
    assertEquals(NonEmptyBatch("a", "b", "c").zipWithIndex == NonEmptyBatch(("a", 0), ("b", 1), ("c", 2)), true)
  }

  given CanEqual[(Int, String), (Int, String)] = CanEqual.derived

  test("NonEmptyBatch ops.should have: zip") {
    assertEquals(
      (NonEmptyBatch(1, 2, 3) `zip` NonEmptyBatch("a", "b", "c")) == NonEmptyBatch((1, "a"), (2, "b"), (3, "c")),
      true
    )
  }

  test("NonEmptyBatch ops.should have: forall") {
    assertEquals(NonEmptyBatch(1, 2, 3).forall(_ > 0), true)
    assertEquals(NonEmptyBatch(1, 2, 3).forall(_ > 1), false)
  }

  test("NonEmptyBatch ops.should have: find") {
    assertEquals(NonEmptyBatch(1, 2, 3).find(_ == 2), Some(2))
    assertEquals(NonEmptyBatch(1, 2, 3).find(_ == 4), None)
  }

  test("NonEmptyBatch ops.should have: exists") {
    assertEquals(NonEmptyBatch(1, 2, 3).exists(_ == 2), true)
    assertEquals(NonEmptyBatch(1, 2, 3).exists(_ == 4), false)
  }

  test("sequence - Option") {
    val actual =
      NonEmptyBatch(Option(1), None, Option(3))

    val expected =
      Some(NonEmptyBatch(1, 3))

    assertEquals(actual.sequence, expected)
  }

  test("sequence - Outcome") {
    val actual =
      NonEmptyBatch(Outcome(1), Outcome(2), Outcome(3))

    val expected =
      Outcome(NonEmptyBatch(1, 2, 3))

    assertEquals(actual.sequence, expected)
  }

}
