package indigo.shared.datatypes

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

    assertEquals(Matrix4.identity.translate(Vector3(2.0, 0, 0)), expected)

  }

  test("translate y") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 2, 0, 1)
      )

    assertEquals(Matrix4.identity.translate(Vector3(0, 2.0, 0)), expected)

  }

  test("translate z") {

    val expected =
      Matrix4(
        (1, 0, 0, 0),
        (0, 1, 0, 0),
        (0, 0, 1, 0),
        (0, 0, 2, 1)
      )

    assertEquals(Matrix4.identity.translate(Vector3(0, 0, 2.0)), expected)

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

    assertEquals(Matrix4.identity.rotate(Radians.PI), expected)
  }

  test("scale") {

    val expected =
      Matrix4(
        (2, 0, 0, 0),
        (0, 3, 0, 0),
        (0, 0, 4, 0),
        (0, 0, 0, 1)
      )

    assertEquals(Matrix4.identity.scale(Vector3(2.0, 3.0, 4.0)), expected)

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

  test("transforming vectors - translation") {
    val mat: Matrix4 =
      Matrix4.identity.translate(Vector3(10.0, 20.0, 30.0))

    val actual: Vector3 =
      mat.transform(Vector3(5.0, 5.0, 5.0))

    val expected: Vector3 =
      Vector3(15.0, 25.0, 35.0)

    assertEquals(actual, expected)
  }

  test("transforming vectors - rotation") {
    val mat: Matrix4 =
      Matrix4.identity.rotate(Radians.TAUby2)

    val actual: Vector3 =
      mat.transform(Vector3(5.0, 5.0, 5.0))

    val expected: Vector3 =
      Vector3(-5.0, -5.0, 5.0)

    assert(actual ~== expected)
  }

  test("transforming vectors - scaling") {
    val mat: Matrix4 =
      Matrix4.identity.scale(Vector3(10.0, 20.0, 30.0))

    val actual: Vector3 =
      mat.transform(Vector3(5.0, 5.0, 5.0))

    val expected: Vector3 =
      Vector3(50.0, 100.0, 150.0)

    assertEquals(actual, expected)
  }

  test("transforming vectors - translate (forward) and rotation") {
    val mat: Matrix4 =
      Matrix4.identity
        .translate(Vector3(0, 8, 0))
        .rotate(Radians.TAUby4.negative)

    val actual: Vector3 =
      mat.transform(Vector3(0, 0, 0))

    val expected: Vector3 =
      Vector3(8, 0, 0)

    assert(clue(actual) ~== clue(expected))
  }

  test("transforming vectors - translate (backwards) and rotation") {
    val mat: Matrix4 =
      Matrix4.identity
        .translate(Vector3(-2, -2, 0))
        .rotate(Radians.TAUby4.negative)

    val actual: Vector3 =
      mat.transform(Vector3(2, 10, 0))

    val expected: Vector3 =
      Vector3(8, 0, 0)

    assert(clue(actual) ~== clue(expected))
  }

  test("approx equals") {
    val mat1: Matrix4 =
      Matrix4.identity
        .rotate(Radians.TAUby4)

    val mat2: Matrix4 =
      Matrix4.identity
        .rotate(Radians(Radians.TAUby4.value + 0.0005))

    assert(clue(mat1) ~== clue(mat2))
  }

}
