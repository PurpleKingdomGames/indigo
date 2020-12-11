package indigo.shared.display

import indigo.shared.datatypes.Matrix4

class Matrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 0, 1)
      )

    assertEquals(Matrix4.identity, expected)

  }

  test("translate x") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (2, 0, 0, 1)
      )

    assertEquals(Matrix4.identity.translate(2.0, 0, 0), expected)

  }

  test("translate y") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 2, 0, 1)
      )

    assertEquals(Matrix4.identity.translate(0, 2.0, 0), expected)

  }

  test("translate z") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 2, 1)
      )

    assertEquals(Matrix4.identity.translate(0, 0, 2.0), expected)

  }

  test("rotation") {

    val s = Math.sin(Math.PI)
    val c = Math.cos(Math.PI)

    val expected =
      Matrix4(
        (c, s, 0, 0),
        (-s, c, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 0, 1)
      )

    assertEquals(Matrix4.identity.rotate(Math.PI), expected)
  }

  test("scale") {

    val expected =
      Matrix4(
        (2, 0, 0, 0),
        (0, 3, 0, 0),
        (0, 0, 4, 0),
        (0, 0, 0, 1)
      )

    assertEquals(Matrix4.identity.scale(2.0, 3.0, 4.0), expected)

  }

  test("transpose") {

    val mat =
      Matrix4(
        (1, 2, 3, 4),
        (5, 6, 7, 8),
        (9, 10, 11, 12),
        (13, 14, 15, 16)
      )

    val expected =
      Matrix4(
        (1, 5, 9, 13),
        (2, 6, 10, 14),
        (3, 7, 11, 15),
        (4, 8, 12, 16)
      )

    assertEquals(mat.transpose, expected)
  }

  test("multiply") {
    val mat1: Matrix4 =
      Matrix4(
        (1, 2, 3, 4),
        (2, 1, 2, 3),
        (3, 2, 1, 2),
        (4, 3, 2, 1)
      )

    val mat2: Matrix4 =
      Matrix4(
        (10, 20, 30, 40),
        (20, 10, 20, 30),
        (30, 20, 10, 20),
        (40, 30, 20, 10)
      )

    val expected: Matrix4 =
      Matrix4(
        (300, 220, 180, 200),
        (220, 180, 160, 180),
        (180, 160, 180, 220),
        (200, 180, 220, 300)
      )

    val actual: Matrix4 =
      mat1 * mat2

    assertEquals(actual, expected)
  }

  test("flip") {
    val mat =
      Matrix4(
        (1, 2, 3, 4),
        (5, 6, 7, 8),
        (9, 10, 11, 12),
        (13, 14, 15, 16)
      )

    val expected =
      Matrix4(
        (-1, -2, -3, 4),
        (-5, -6, -7, 8),
        (-9, -10, -11, 12),
        (-13, -14, -15, 16)
      )

    assertEquals(mat.flip(true, true), expected)
  }

}
