package indigo.shared.datatypes.mutable

import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Matrix4

/*
In these tests, Matrix4 is acting as the reference implementation.
The implementations are, however, intentionally not identical, so
we're saying that the cheap version needs to say what the real one
says, where applicable. Full multiplication for example, isn't
identical (but that's not a problem for our use case).
 */
class CheapCheapMatrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected =
      Matrix4.identity

    assertEquals(CheapMatrix4.identity.toMatrix4, expected)

  }

  test("translate x") {

    val expected =
      Matrix4.identity.translate(2.0, 0, 0)

    assertEquals(CheapMatrix4.identity.translate(2.0, 0, 0).toMatrix4, expected)

  }

  test("translate y") {

    val expected =
      Matrix4.identity.translate(0, 2.0, 0)

    assertEquals(CheapMatrix4.identity.translate(0, 2.0, 0).toMatrix4, expected)

  }

  test("translate z") {

    val expected =
      Matrix4.identity.translate(0, 0, 2.0)

    assertEquals(CheapMatrix4.identity.translate(0, 0, 2.0).toMatrix4, expected)

  }

  test("rotation") {

    val expected =
      Matrix4.identity.rotate(Radians.PI)

    assertEquals(CheapMatrix4.identity.rotate(Radians.PI).toMatrix4, expected)
  }

  test("scale") {

    val expected =
      Matrix4.identity.scale(2.0, 3.0, 1.0)

    assertEquals(CheapMatrix4.identity.scale(2.0, 3.0, 1.0).toMatrix4, expected)

  }

  test("a more realistic transformation") {

    val expected =
      Matrix4.identity
        .scale(2.0, 3.0, 1.0)
        .rotate(Radians.TAUby4)
        .translate(100, 0.0, 0.0)
        .rotate(Radians.TAUby2)

    val actual =
      CheapMatrix4.identity
        .scale(2.0, 3.0, 1.0)
        .rotate(Radians.TAUby4)
        .translate(100, 0.0, 0.0)
        .rotate(Radians.TAUby2)

    assertEquals(actual.toMatrix4, expected)

  }

  test("orthographic") {

    val expected =
      Matrix4.orthographic(320, 240)

    val actual =
      CheapMatrix4.orthographic(320, 240)

    assertEquals(actual.toMatrix4, expected)

  }

}
