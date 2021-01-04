package indigo.shared.datatypes.mutable

import indigo.shared.datatypes.Radians

class CheapCheapMatrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected =
      CheapMatrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 0, 1)
      )

    assertEquals(CheapMatrix4.identity.toMatrix4, expected.toMatrix4)

  }

  test("translate x") {

    val expected =
      CheapMatrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (2, 0, 0, 1)
      )

    assertEquals(CheapMatrix4.identity.translate(2.0, 0, 0).toMatrix4, expected.toMatrix4)

  }

  test("translate y") {

    val expected =
      CheapMatrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 2, 0, 1)
      )

    assertEquals(CheapMatrix4.identity.translate(0, 2.0, 0).toMatrix4, expected.toMatrix4)

  }

  test("translate z") {

    val expected =
      CheapMatrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 2, 1)
      )

    assertEquals(CheapMatrix4.identity.translate(0, 0, 2.0).toMatrix4, expected.toMatrix4)

  }

  test("rotation") {

    val s = Math.sin(Math.PI)
    val c = Math.cos(Math.PI)

    val expected =
      CheapMatrix4(
        (c, s, 0, 0),
        (-s, c, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 0, 1)
      )

    assertEquals(CheapMatrix4.identity.rotate(Radians.PI.value).toMatrix4, expected.toMatrix4)
  }

  test("scale") {

    val expected =
      CheapMatrix4(
        (2, 0, 0, 0),
        (0, 3, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 0, 1)
      )

    assertEquals(CheapMatrix4.identity.scale(2.0, 3.0, 1.0).toMatrix4, expected.toMatrix4)

  }

  // test("multiply") {
  //   val mat1: CheapMatrix4 =
  //     CheapMatrix4(
  //       (1, 2, 3, 4),
  //       (2, 1, 2, 3),
  //       (3, 2, 1, 2),
  //       (4, 3, 2, 1)
  //     )

  //   val mat2: CheapMatrix4 =
  //     CheapMatrix4(
  //       (10, 20, 30, 40),
  //       (20, 10, 20, 30),
  //       (30, 20, 10, 20),
  //       (40, 30, 20, 10)
  //     )

  //   val expected: CheapMatrix4 =
  //     CheapMatrix4(
  //       (300, 220, 180, 200),
  //       (220, 180, 160, 180),
  //       (180, 160, 180, 220),
  //       (200, 180, 220, 300)
  //     )

  //   val actual: CheapMatrix4 =
  //     mat1 * mat2

  //   assertEquals(actual.toMatrix4, expected.toMatrix4)
  // }

}
