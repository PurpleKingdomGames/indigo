package indigo.shared.display

import indigo.shared.datatypes.Matrix3

class Matrix3Tests extends munit.FunSuite {

  test("identity") {
    val expected =
      Matrix3(
        (1, 0, 0),
        (0, 1, 0),
        (0, 0, 1)
      )

    assertEquals(Matrix3.identity, expected)

  }

  test("translate x") {

    val matrix =
      Matrix3(
        (1, 0, 0),
        (0, 1, 0),
        (10, 20, 1)
      )

    val expected =
      Matrix3(
        (1, 0, 0),
        (0, 1, 0),
        (150, 20, 1)
      )

    assertEquals(matrix.translate(140.0, 0), expected)

  }

  test("translate y") {

    val expected =
      Matrix3(
        (1, 0, 0),
        (0, 1, 0),
        (0, 2, 1)
      )

    assertEquals(Matrix3.identity.translate(0, 2.0), expected)

  }

  test("rotation") {

    val s = Math.sin(Math.PI)
    val c = Math.cos(Math.PI)

    val expected =
      Matrix3(
        (c, s, 0),
        (-s, c, 0),
        (0, 0, 1)
      )

    assertEquals(Matrix3.identity.rotate(Math.PI), expected)
  }

  test("scale") {

    val expected: Matrix3 =
      Matrix3(
        (2, 0, 0),
        (0, 3, 0),
        (0, 0, 1)
      )

    assertEquals(Matrix3.identity.scale(2.0, 3.0), expected)

  }

  test("transpose") {

    val mat =
      Matrix3(
        (1, 2, 3),
        (4, 5, 6),
        (7, 8, 9)
      )

    val expected =
      Matrix3(
        (1, 4, 7),
        (2, 5, 8),
        (3, 6, 9)
      )

    assertEquals(mat.transpose, expected)
  }

  test("multiply") {
    val mat1: Matrix3 =
      Matrix3(
        (1, 2, 3),
        (2, 1, 2),
        (3, 2, 1)
      )

    val mat2: Matrix3 =
      Matrix3(
        (10, 20, 30),
        (20, 10, 20),
        (30, 20, 10)
      )

    val expected: Matrix3 =
      Matrix3(
        (140, 100, 100),
        (100, 90, 100),
        (100, 100, 140)
      )

    val actual: Matrix3 =
      mat1 * mat2

    assertEquals(actual, expected)
  }

  test("flip") {
    val mat =
      Matrix3(
        (1, 2, 3),
        (4, 5, 6),
        (7, 8, 9)
      )

    val expected =
      Matrix3(
        (-1, -2, 3),
        (-4, -5, 6),
        (-7, -8, 9)
      )

    assertEquals(mat.flip(true, true), expected)
  }

}
