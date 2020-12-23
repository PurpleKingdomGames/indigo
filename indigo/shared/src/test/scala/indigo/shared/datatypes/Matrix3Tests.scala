package indigo.shared.datatypes

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

    assertEquals(matrix.translate(Vector2(140.0, 0)), expected)

  }

  test("translate y") {

    val expected =
      Matrix3(
        (1, 0, 0),
        (0, 1, 0),
        (0, 2, 1)
      )

    assertEquals(Matrix3.identity.translate(Vector2(0, 2.0)), expected)

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

    assertEquals(Matrix3.identity.rotate(Radians.PI), expected)
  }

  test("scale") {

    val expected: Matrix3 =
      Matrix3(
        (2, 0, 0),
        (0, 3, 0),
        (0, 0, 1)
      )

    assertEquals(Matrix3.identity.scale(Vector2(2.0, 3.0)), expected)

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

  test("transforming vectors - translation (forwards)") {
    val mat: Matrix3 =
      Matrix3.identity.translate(Vector2(10.0, 20.0))

    val actual: Vector2 =
      mat.transform(Vector2(5.0, 5.0))

    val expected: Vector2 =
      Vector2(15.0, 25.0)

    assertEquals(actual, expected)
  }

  test("transforming vectors - translation (backwards)") {
    val mat: Matrix3 =
      Matrix3.identity.translate(Vector2(-10.0, -10.0))

    val actual: Vector2 =
      mat.transform(Vector2(10.0, 20.0))

    val expected: Vector2 =
      Vector2(0.0, 10.0)

    assertEquals(actual, expected)
  }

  test("transforming vectors - rotation") {
    val mat: Matrix3 =
      Matrix3.identity.rotate(Radians.TAUby2)

    val actual: Vector2 =
      mat.transform(Vector2(5.0, 5.0))

    val expected: Vector2 =
      Vector2(-5.0, -5.0)

    assert(clue(actual) ~== clue(expected))
  }

  test("transforming vectors - scaling") {
    val mat: Matrix3 =
      Matrix3.identity.scale(Vector2(10.0, 20.0))

    val actual: Vector2 =
      mat.transform(Vector2(5.0, 5.0))

    val expected: Vector2 =
      Vector2(50.0, 100.0)

    assertEquals(actual, expected)
  }

  test("transforming vectors - translate forward and backwards give same position") {
    val actual1: Vector2 =
      Matrix3.identity
        .translate(Vector2(0, 8))
        .transform(Vector2(0, 0))
        
    val actual2: Vector2 =
      Matrix3.identity
        .translate(Vector2(-2, -2))
        .transform(Vector2(2, 10))

    val expected: Vector2 =
      Vector2(0, 8)

    assert(clue(actual1) ~== clue(expected))
    assert(clue(actual2) ~== clue(expected))
  }

  test("transforming vectors - translate (forward) and rotation") {
    val mat: Matrix3 =
      Matrix3.identity
        .translate(Vector2(0, 8))
        .rotate(Radians.TAUby4.negative)

    val actual: Vector2 =
      mat.transform(Vector2(0, 0))

    val expected: Vector2 =
      Vector2(8, 0)

    assert(clue(actual) ~== clue(expected))
  }

  test("transforming vectors - translate (backwards) and rotation") {
    val mat: Matrix3 =
      Matrix3.identity
        .translate(Vector2(-2, -2))
        .rotate(Radians.TAUby4.negative)

    val actual: Vector2 =
      mat.transform(Vector2(2, 10))

    val expected: Vector2 =
      Vector2(8, 0)

    assert(clue(actual) ~== clue(expected))
  }

}
