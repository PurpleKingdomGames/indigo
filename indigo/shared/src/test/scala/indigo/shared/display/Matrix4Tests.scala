package indigo.shared.display

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Matrix4

class Matrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected: List[Double] = List(
      1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1
    )

    assertEquals(Matrix4.identity.mat, expected)

  }

  test("translate x") {

    val expected: List[Double] = List(
      1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 2, 0, 0, 1
    )

    assertEquals(Matrix4.identity.translate(2.0, 0, 0).mat, expected)

  }

  test("translate y") {

    val expected: List[Double] = List(
      1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 2, 0, 1
    )

    assertEquals(Matrix4.identity.translate(0, 2.0, 0).mat, expected)

  }

  test("translate z") {

    val expected: List[Double] = List(
      1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 2, 1
    )

    assertEquals(Matrix4.identity.translate(0, 0, 2.0).mat, expected)

  }

  test("rotation") {

    val s = Math.sin(Math.PI)
    val c = Math.cos(Math.PI)

    val expected: List[Double] = List(
      c,
      s,
      0,
      0,
      -s,
      c,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1
    )

    assertEquals(Matrix4.identity.rotate(Math.PI).mat, expected)
  }

  test("scale") {

    val expected: List[Double] = List(
      2, 0, 0, 0, 0, 3, 0, 0, 0, 0, 4, 0, 0, 0, 0, 1
    )

    assertEquals(Matrix4.identity.scale(2.0, 3.0, 4.0).mat, expected)

  }

  test("transpose") {

    val mat: List[Double] =
      List(
        1, 2, 3, 4,
        5, 6, 7, 8,
        9, 10, 11, 12,
        13, 14, 15, 16
      )

    val expected: List[Double] =
      List(
        1, 5, 9, 13,
        2, 6, 10, 14,
        3, 7, 11, 15,
        4, 8, 12, 16
      )

    assertEquals(Matrix4(mat).transpose.mat, expected)
  }

  test("multiply") {
    //
  }

  test("flip") {
    //
  }

  test("toMatrix3") {
    //
  }

  test("projection") {
    //
  }

  test("orthographic") {
    //
  }

  test("withOrthographic") {
    //
  }

  test("transform2d") {
    //
  }

  test("rotate x") {
    //
  }

  test("rotate y") {
    //
  }

  test("rotate z") {
    //
  }

  test("translateAndScale") {
    //
  }

}
