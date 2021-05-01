package indigo.shared.collections

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
class NonEmptyListTests extends munit.FunSuite {

  test("NonEmptyList AsString should be able to show a list of Ints") {

    val nel = NonEmptyList(1, 2, 3)

    assertEquals(nel.toString(), "NonEmptyList[1][2, 3]")

  }

  test("NonEmptyList usage can be pattern matched") {
    NonEmptyList(1, 2, 3) match {
      case NonEmptyList(head, tail) =>
        assertEquals(head, 1)
        assertEquals(tail, List(2, 3))

      case null =>
        fail("shouldn't have got here")
    }
  }

  test("NonEmptyList ops.should have: equality") {
    assertEquals(NonEmptyList(1) == NonEmptyList(1), true)
  }

  test("NonEmptyList ops.should have: point") {
    assertEquals(NonEmptyList(1) == NonEmptyList(1), true)
  }

  test("NonEmptyList ops.should have: map") {
    assertEquals(NonEmptyList(1, 2, 3).map(_ * 10) == NonEmptyList(10, 20, 30), true)
  }

  test("NonEmptyList ops.should have: reverse") {
    assertEquals(NonEmptyList(1, 2, 3, 4, 5).reverse == NonEmptyList(5, 4, 3, 2, 1), true)
    assertEquals(NonEmptyList(1) == NonEmptyList(1), true)
  }

  test("NonEmptyList ops.should have: combine") {
    assertEquals(NonEmptyList(1) ++ NonEmptyList(2) == NonEmptyList(1, 2), true)
  }

  test("NonEmptyList ops.should have: flatten") {
    assertEquals(NonEmptyList.flatten(NonEmptyList(NonEmptyList.point(1))) == NonEmptyList(1), true)
  }

  test("NonEmptyList ops.should have: flatMap") {
    assertEquals(NonEmptyList(1, 2, 3).flatMap(i => NonEmptyList(i * 10 + 1)) == NonEmptyList(11, 21, 31), true)
  }

  test("NonEmptyList ops.should have: foldLeft") {
    assertEquals(NonEmptyList("a", "b", "c").foldLeft("")(_ + _), "abc")
  }

  test("NonEmptyList ops.should have: reduceLeft") {
    assertEquals(NonEmptyList("a", "b", "c").reduce(_ + _), "abc")
  }

  test("NonEmptyList ops.should have: append") {
    assertEquals(NonEmptyList(1) :+ 2 == NonEmptyList(1, 2), true)
  }

  test("NonEmptyList ops.should have: cons") {
    assertEquals(1 :: NonEmptyList(2) == NonEmptyList(1, 2), true)
  }

  given CanEqual[(String, Int), (String, Int)] = CanEqual.derived

  test("NonEmptyList ops.should have: zipWithIndex") {
    assertEquals(NonEmptyList("a", "b", "c").zipWithIndex == NonEmptyList(("a", 0), ("b", 1), ("c", 2)), true)
  }

  given CanEqual[(Int, String), (Int, String)] = CanEqual.derived

  test("NonEmptyList ops.should have: zip") {
    assertEquals(
      (NonEmptyList(1, 2, 3) zip NonEmptyList("a", "b", "c")) == NonEmptyList((1, "a"), (2, "b"), (3, "c")),
      true
    )
  }

  test("NonEmptyList ops.should have: forall") {
    assertEquals(NonEmptyList(1, 2, 3).forall(_ > 0), true)
    assertEquals(NonEmptyList(1, 2, 3).forall(_ > 1), false)
  }

  test("NonEmptyList ops.should have: find") {
    assertEquals(NonEmptyList(1, 2, 3).find(_ == 2), Some(2))
    assertEquals(NonEmptyList(1, 2, 3).find(_ == 4), None)
  }

  test("NonEmptyList ops.should have: exists") {
    assertEquals(NonEmptyList(1, 2, 3).exists(_ == 2), true)
    assertEquals(NonEmptyList(1, 2, 3).exists(_ == 4), false)
  }

}
