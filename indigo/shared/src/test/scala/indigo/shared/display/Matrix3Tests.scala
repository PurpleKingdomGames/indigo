package indigo.shared.display

import indigo.shared.EqualTo._
import indigo.shared.datatypes.Matrix3

class Matrix3Tests extends munit.FunSuite {

  test("identity") {

    val expected: List[Double] = List(
      1, 0, 0,
      0, 1, 0,
      0, 0, 1
    )

    assertEquals(Matrix3.identity.mat, expected)

  }

  test("translate x") {

    val expected: List[Double] = List(
      1, 0, 0,
      0, 1, 0,
      2, 0, 1
    )

    assertEquals(Matrix3.identity.translate(2.0, 0).mat, expected)

  }

  test("translate y") {

    val expected: List[Double] = List(
      1, 0, 0,
      0, 1, 0,
      0, 2, 1
    )

    assertEquals(Matrix3.identity.translate(0, 2.0).mat, expected)

  }

  test("rotation") {

    val s = Math.sin(Math.PI)
    val c = Math.cos(Math.PI)

    val expected: List[Double] = List(
      c,s,0,
      -s,c,0,
      0,0,1
    )

    assertEquals(Matrix3.identity.rotate(Math.PI).mat, expected)
  }

  test("scale") {

    val expected: List[Double] = List(
      2, 0, 0,
      0, 3, 0,
      0, 0, 1
    )

    assertEquals(Matrix3.identity.scale(2.0, 3.0).mat, expected)

  }

  test("transpose") {

    val mat: List[Double] =
      List(
        1, 2, 3,
        4, 5, 6,
        7, 8, 9
      )

    val expected: List[Double] =
      List(
        1, 4, 7,
        2, 5, 8,
        3, 6, 9
      )

    assertEquals(Matrix3(mat).transpose.mat, expected)
  }

  test("multiply") {
    //
  }

  test("flip") {
    //
  }

  test("toMatrix4") {
    //
  }

}
