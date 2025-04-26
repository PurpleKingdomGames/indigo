package indigo.shared.datatypes

import indigo.shared.time.Seconds

class RadiansTests extends munit.FunSuite {

  test("constants are equivalent") {
    assertEquals(Radians.TAU, Radians.`2PI`)
    assertEquals(Radians.TAUby2, Radians.PI)
    assertEquals(Radians.TAUby4, Radians.PIby2)
  }

  test("Can make a Radians instance from degrees") {

    assert(Radians.fromDegrees(0) ~== Radians.zero)
    assert(Radians.fromDegrees(180) ~== Radians.PI)
    assert(clue(Radians.fromDegrees(359)) ~== clue(clue(Radians.TAU) - Radians(0.0175d)))
    assert(Radians.fromDegrees(360) ~== Radians.zero)
    assert(clue(Radians.fromDegrees(-90)) ~== (-Radians.PI / 2))

  }

  test("Can convert seconds to Radians") {
    assert(Radians.fromSeconds(Seconds(0)) ~== Radians.zero)
    assert(Radians.fromSeconds(Seconds(0.5)) ~== Radians.PI)
    assert(Radians.fromSeconds(Seconds(1)) ~== Radians.zero)
    assert(Radians.fromSeconds(Seconds(1.5)) ~== Radians.PI)
  }

  test("Wrap Radians") {
    assert(Radians(0.0).wrap ~== Radians(0.0))
    assert(Radians(0.1).wrap ~== Radians(0.1))
    assert(Radians(-0.1).wrap ~== Radians.TAU - Radians(0.1))
    assert((Radians.TAU + Radians.TAUby4).wrap ~== Radians.TAUby4)
    assert((Radians.TAU - Radians.TAUby4).wrap ~== Radians.TAUby4 * Radians(3))
  }

  test("Wrap Radians (centered)") {
    assert(Radians(0.0).centeredWrap ~== Radians(0.0))
    assert(Radians(0.1).centeredWrap ~== Radians(0.1))
    assert(Radians(-0.1).centeredWrap ~== Radians(-0.1))
    assert((Radians.TAU + Radians.TAUby4).centeredWrap ~== Radians.TAUby4)
    assert((Radians.TAU - Radians.TAUby4).centeredWrap ~== -Radians.PIby2)
  }

  test("convert to degrees") {
    assert(doubleCloseEnough(Radians.zero.toDegrees, 0.0d))
    assert(doubleCloseEnough(Radians.PI.toDegrees, 180.0d))
    assert(doubleCloseEnough(Radians.TAU.toDegrees, 360.0d))
    assert(doubleCloseEnough(clue(Radians(0.25).toDegrees), clue(14.32d)))
  }

  test("mod") {
    assert(Radians.mod(Radians(11), Radians(10)) ~== Radians(1))
    assert(Radians(11) % Radians(10) ~== Radians(1))
    assert(Radians.mod(Radians(9), Radians(10)) ~== Radians(9))
    assert(Radians.mod(Radians(1), Radians(10)) ~== Radians(1))
    assert(Radians.mod(Radians(-11), Radians(10)) ~== Radians(9))
    assert(Radians.mod(Radians(-1), Radians(10)) ~== Radians(9))
    assert(Radians.mod(Radians(0), Radians(10)) ~== Radians(0))
    assert(clue(Radians.mod(Radians(-11), Radians(-10))) ~== clue(Radians(-1)))
  }

  test("max") {
    assert(clue(Radians.PI.max(Radians.zero)) ~== Radians.PI)
  }

  test("min") {
    assert(clue(Radians.PI.min(Radians.zero)) ~== -Radians.zero)
  }

  def doubleCloseEnough(r1: Double, r2: Double): Boolean =
    r1 - 0.01 < r2 && r1 + 0.01 > r2

}
