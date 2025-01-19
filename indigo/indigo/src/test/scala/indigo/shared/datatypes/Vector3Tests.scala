package indigo.shared.datatypes

class Vector3Tests extends munit.FunSuite {

  test("Basic vector operation.should be able to divide") {
    assert(Vector3(10, 10, 10) / Vector3(2, 5, 10) ~== Vector3(5, 2, 1))
  }

  test("Basic vector operation.distance function") {
    assertEquals(to2dp(Vector3.distance(Vector3(1, 2, 3), Vector3(5, 6, 7))), 6.93)
    assertEquals(to2dp(Vector3.distance(Vector3(-6, 8, 1), Vector3(-3, 9, 1))), 3.16)
  }

  test("Basic vector operation.should be able to scale and translate") {

    val actual =
      Vector3(10, 10, 10).scaleBy(Vector3(2, 2, 2)).translate(Vector3(5, 5, 5))

    val expected =
      Vector3(25)

    assertEquals(actual, expected)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector3s.parallel") {
    assertEquals(Vector3(0, 0, 0) `dot` Vector3(0, 0, 0), 0.0)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector3s.facing") {
    assertEquals((Vector3(2, 2, 2) `dot` Vector3(-1, -1, -1)) < 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector3s.not facing") {
    assertEquals((Vector3(2, 2, 2) `dot` Vector3(1, 1, 1)) > 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector3s.value") {
    assertEquals(Math.round(Vector3(-6.0, 8.0, 1) `dot` Vector3(5.0, 12.0, 2)).toDouble, 68d)
  }

  test("dot product") {
    assert(clue(Vector3(1, 0, 0).dot(Vector3(1, 0, 0))) == 1)
    assert(clue(Vector3(1, 0, 0).dot(Vector3(0, -1, 0))) == 0)
    assert(clue(Vector3(-1, 0, 0).dot(Vector3(1, 0, 0))) == -1)
    assert(clue(Vector3(1, 0, 0).dot(Vector3(-1, 0, 0))) == -1)
  }

  test("abs") {
    assertEquals(Vector3(1, 1, 1).abs, Vector3(1, 1, 1))
    assertEquals(Vector3(1, -1, 1).abs, Vector3(1, 1, 1))
    assertEquals(Vector3(-1, -1, -1).abs, Vector3(1, 1, 1))
  }

  test("min") {
    assertEquals(Vector3(10, 10, 10).min(1), Vector3(1, 1, 1))
    assertEquals(Vector3(10, 10, 10).min(100), Vector3(10, 10, 10))
    assertEquals(Vector3(10, 10, 10).min(Vector3(50, 5, 2)), Vector3(10, 5, 2))
  }

  test("max") {
    assertEquals(Vector3(10, 10, 10).max(1), Vector3(10, 10, 10))
    assertEquals(Vector3(10, 10, 10).max(100), Vector3(100, 100, 100))
    assertEquals(Vector3(10, 10, 10).max(Vector3(50, 5, 2)), Vector3(50, 10, 10))
  }

  test("clamp - Double") {
    assertEquals(Vector3(0.1, 0.1, 0.1).clamp(0, 1), Vector3(0.1, 0.1, 0.1))
    assertEquals(Vector3(-0.1, 1.1, 0.1).clamp(0, 1), Vector3(0.0, 1.0, 0.1))
    assertEquals(Vector3(1, 4, 5).clamp(2, 3), Vector3(2, 3, 3))
  }

  test("clamp - Vector3") {
    assertEquals(Vector3(0.1, 0.1, 0.1).clamp(Vector3(0), Vector3(1)), Vector3(0.1, 0.1, 0.1))
    assertEquals(Vector3(-0.1, 1.1, 0.1).clamp(Vector3(0), Vector3(1)), Vector3(0.0, 1.0, 0.1))
    assertEquals(Vector3(1, 4, 5).clamp(Vector3(2), Vector3(3)), Vector3(2, 3, 3))
    assertEquals(Vector3(-2, 2, -2).clamp(Vector3(-1), Vector3(1)), Vector3(-1, 1, -1))
    assertEquals(Vector3(-2, 2, 2).clamp(Vector3(-1, 0, 0), Vector3(0, 1, 5)), Vector3(-1, 1, 2))
  }

  test("length") {
    assertEquals(Vector3(10, 0, 0).length, 10.0)
    assertEquals(Vector3(0, 0, 10).length, 10.0)
    assert(nearEnoughEqual(clue(Vector3(10, 10, 10).length), 17.320, 0.01))
  }

  test("invert") {
    assertEquals(Vector3(1d, 1d, 1d).invert, Vector3(-1d, -1d, -1d))
    assertEquals(Vector3(-1d, 2d, -3d).invert, Vector3(1d, -2d, 3d))
    assertEquals(Vector3(-1d, -1d, -1d).invert, Vector3(1d, 1d, 1d))
  }

  test("translate | moveBy | moveTo") {
    assertEquals(Vector3(1, 1, 1).translate(Vector3(10, 10, 10)), Vector3(11, 11, 11))
    assertEquals(Vector3(1, 1, 1).moveBy(Vector3(10, 10, 10)), Vector3(11, 11, 11))
    assertEquals(Vector3(1, 1, 1).moveTo(Vector3(10, 10, 10)), Vector3(10, 10, 10))
  }

  test("scaleBy") {
    assertEquals(Vector3(2, 2, 10).scaleBy(Vector3(10, 2, 3)), Vector3(20, 4, 30))
  }

  test("ceil") {
    assertEquals(Vector3(2.2, 2.6, 5.1).ceil, Vector3(3, 3, 6))
  }

  test("floor") {
    assertEquals(Vector3(2.2, 2.6, 5.1).floor, Vector3(2, 2, 5))
  }

  test("round") {
    assertEquals(Vector3(2.2, 2.6, 5.1).round, Vector3(2, 3, 5))
  }

  test("normalise") {
    assert(clue(Vector3(10, 10, 10).normalise) ~== clue(Vector3(0.5773, 0.5773, 0.5773)))
    assert(clue(Vector3(-10, -10, -10).normalise) ~== clue(Vector3(-0.5773, -0.5773, -0.5773)))
    assert(clue(Vector3(10, 0, 0).normalise) == clue(Vector3(1, 0, 0)))
    assert(clue(Vector3(0, 0, 0).normalise) == clue(Vector3(0, 0, 0)))
    assert(clue(Vector3(-50, 1000, 1000).normalise) ~== clue(Vector3(-0.0353, 0.7066, 0.7066)))
  }

  test("approx equal") {
    assert(Vector3(5.0, 5.0, 5.0) ~== Vector3(4.999999, 5.00001, 5.0))
    assert(!(Vector3(5.0, 5.0, 5.0) ~== Vector3(-4.999999, 5.00001, 5.0)))
  }

  test("mod") {
    assert(Vector3.mod(Vector3(11, 12, 13), Vector3(10)) ~== Vector3(1, 2, 3))
    assert(Vector3(11, 12, 13) % Vector3(10) ~== Vector3(1, 2, 3))
    assert(Vector3.mod(Vector3(9, 10, 11), Vector3(10)) ~== Vector3(9, 0, 1))
    assert(Vector3.mod(Vector3(1), Vector3(10)) ~== Vector3(1))
    assert(Vector3.mod(Vector3(-11, -12, -13), Vector3(10)) ~== Vector3(9, 8, 7))
    assert(Vector3.mod(Vector3(-1), Vector3(10)) ~== Vector3(9))
    assert(Vector3.mod(Vector3(0), Vector3(10)) ~== Vector3(0))
    assert(clue(Vector3.mod(Vector3(-11), Vector3(-10))) ~== clue(Vector3(-1)))
  }

  test("cross product") {
    assertEquals(Vector3.unitX.cross(Vector3.unitY), Vector3.unitZ)
    assertEquals(Vector3.unitY.cross(Vector3.unitZ), Vector3.unitX)
    assertEquals(Vector3.unitX.cross(Vector3.unitZ), -Vector3.unitY)
  }

  def to2dp(d: Double): Double =
    Math.round(d * 100).toDouble / 100

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

}
