package indigo.shared.datatypes.mutable

import indigo.shared.datatypes.Matrix4
import indigo.shared.datatypes.Radians

/*
In these tests, Matrix4 is acting as the reference implementation.
The implementations are, however, intentionally not identical, so
we're saying that the cheap version needs to say what the real one
says, where applicable. Full multiplication for example, isn't
identical (but that's not a problem for our use case).
 */
class CheapMatrix4Tests extends munit.FunSuite {

  test("identity") {

    val expected =
      Matrix4.identity

    assert(clue(CheapMatrix4.identity.toMatrix4) ~== clue(expected))

  }

  test("translate x") {

    val expected =
      Matrix4.identity.translate(2.0, 0, 0)

    assert(clue(CheapMatrix4.identity.translate(2.0, 0, 0).toMatrix4) ~== clue(expected))

  }

  test("translate y") {

    val expected =
      Matrix4.identity.translate(0, 2.0, 0)

    assert(clue(CheapMatrix4.identity.translate(0, 2.0, 0).toMatrix4) ~== clue(expected))

  }

  test("translate z") {

    val expected =
      Matrix4.identity.translate(0, 0, 2.0)

    assert(clue(CheapMatrix4.identity.translate(0, 0, 2.0).toMatrix4) ~== clue(expected))

  }

  test("rotation") {

    val expected =
      Matrix4.identity.rotate(Radians.PI)

    assert(clue(CheapMatrix4.identity.rotate(Radians.PI.toFloat).toMatrix4) ~== clue(expected))
  }

  test("scale") {

    val expected =
      Matrix4.identity.scale(2.0, 3.0, 1.0)

    assert(clue(CheapMatrix4.identity.scale(2.0, 3.0, 1.0).toMatrix4) ~== clue(expected))

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
        .rotate(Radians.TAUby4.toFloat)
        .translate(100, 0.0, 0.0)
        .rotate(Radians.TAUby2.toFloat)

    assert(clue(actual.toMatrix4) ~== clue(expected))

  }

  test("orthographic") {

    val expected =
      Matrix4.orthographic(320, 240)

    val actual =
      CheapMatrix4.orthographic(320, 240)

    assert(clue(actual.toMatrix4) ~== clue(expected))

  }

}
