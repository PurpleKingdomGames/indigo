package indigo.shared.datatypes

class Vector4Tests extends munit.FunSuite {

  test("Basic vector operation.should be able to divide") {
    assert(Vector4(10, 10, 10, 10) / Vector4(2, 5, 10, 1) ~== Vector4(5, 2, 1, 10))
  }

  test("Basic vector operation.distance function") {
    assertEquals(to2dp(Vector4.distance(Vector4(1, 2, 3, 4), Vector4(5, 6, 7, 8))), 8.0)
    assertEquals(to2dp(Vector4.distance(Vector4(-6, 8, 1, 1), Vector4(-3, 9, 1, 1))), 3.16)
  }

  test("Basic vector operation.should be able to scale and translate") {

    val actual =
      Vector4(10, 10, 10, 10).scaleBy(Vector4(2, 2, 2, 2)).translate(Vector4(5, 5, 5, 5))

    val expected =
      Vector4(25)

    assertEquals(actual, expected)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector4s.parallel") {
    assertEquals(Vector4(0, 0, 0, 0) `dot` Vector4(0, 0, 0, 0), 0.0)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector4s.facing") {
    assertEquals((Vector4(2, 2, 2, 2) `dot` Vector4(-1, -1, -1, -1)) < 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector4s.not facing") {
    assertEquals((Vector4(2, 2, 2, 2) `dot` Vector4(1, 1, 1, 1)) > 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector4s.value") {
    assertEquals(Math.round(Vector4(-6.0, 8.0, 1, 1) `dot` Vector4(5.0, 12.0, 2, 2)).toDouble, 70d)
  }

  test("dot product") {
    assert(clue(Vector4(1, 0, 0, 0).dot(Vector4(1, 0, 0, 0))) == 1)
    assert(clue(Vector4(1, 0, 0, 0).dot(Vector4(0, -1, 0, 0))) == 0)
    assert(clue(Vector4(-1, 0, 0, 0).dot(Vector4(1, 0, 0, 0))) == -1)
    assert(clue(Vector4(1, 0, 0, 0).dot(Vector4(-1, 0, 0, 0))) == -1)
  }

  test("abs") {
    assertEquals(Vector4(1, 1, 1, 1).abs, Vector4(1, 1, 1, 1))
    assertEquals(Vector4(1, -1, 1, -1).abs, Vector4(1, 1, 1, 1))
    assertEquals(Vector4(-1, -1, -1, -1).abs, Vector4(1, 1, 1, 1))
  }

  test("min") {
    assertEquals(Vector4(10, 10, 10, 10).min(1), Vector4(1, 1, 1, 1))
    assertEquals(Vector4(10, 10, 10, 10).min(100), Vector4(10, 10, 10, 10))
    assertEquals(Vector4(10, 10, 10, 10).min(Vector4(50, 5, 2, 100)), Vector4(10, 5, 2, 10))
  }

  test("max") {
    assertEquals(Vector4(10, 10, 10, 10).max(1), Vector4(10, 10, 10, 10))
    assertEquals(Vector4(10, 10, 10, 10).max(100), Vector4(100, 100, 100, 100))
    assertEquals(Vector4(10, 10, 10, 10).max(Vector4(50, 5, 2, 100)), Vector4(50, 10, 10, 100))
  }

  test("clamp - Double") {
    assertEquals(Vector4(0.1, 0.1, 0.1, 0.1).clamp(0, 1), Vector4(0.1, 0.1, 0.1, 0.1))
    assertEquals(Vector4(-0.1, 1.1, 0.1, 0.1).clamp(0, 1), Vector4(0.0, 1.0, 0.1, 0.1))
    assertEquals(Vector4(1, 4, 5, 0).clamp(2, 3), Vector4(2, 3, 3, 2))
  }

  test("clamp - Vector4") {
    assertEquals(Vector4(0.1, 0.1, 0.1, 0.1).clamp(Vector4(0), Vector4(1)), Vector4(0.1, 0.1, 0.1, 0.1))
    assertEquals(Vector4(-0.1, 1.1, 0.1, 0.1).clamp(Vector4(0), Vector4(1)), Vector4(0.0, 1.0, 0.1, 0.1))
    assertEquals(Vector4(1, 4, 5, 0).clamp(Vector4(2), Vector4(3)), Vector4(2, 3, 3, 2))
    assertEquals(Vector4(-2, 2, -2, 2).clamp(Vector4(-1), Vector4(1)), Vector4(-1, 1, -1, 1))
    assertEquals(Vector4(-2, 2, 2, -2).clamp(Vector4(-1, 0, 0, 0), Vector4(0, 1, 5, 1)), Vector4(-1, 1, 2, 0))
  }

  test("length") {
    assertEquals(Vector4(10, 0, 0, 0).length, 10.0)
    assertEquals(Vector4(0, 0, 0, 10).length, 10.0)
    assert(nearEnoughEqual(clue(Vector4(10, 10, 10, 10).length), 20d, 0.01))
  }

  test("invert") {
    assertEquals(Vector4(1d, 1d, 1d, 1d).invert, Vector4(-1d, -1d, -1d, -1d))
    assertEquals(Vector4(-1d, 2d, -3d, 4d).invert, Vector4(1d, -2d, 3d, -4d))
    assertEquals(Vector4(-1d, -1d, -1d, -1d).invert, Vector4(1d, 1d, 1d, 1d))
  }

  test("translate | moveBy | moveTo") {
    assertEquals(Vector4(1, 1, 1, 1).translate(Vector4(10, 10, 10, 10)), Vector4(11, 11, 11, 11))
    assertEquals(Vector4(1, 1, 1, 1).moveBy(Vector4(10, 10, 10, 10)), Vector4(11, 11, 11, 11))
    assertEquals(Vector4(1, 1, 1, 1).moveTo(Vector4(10, 10, 10, 10)), Vector4(10, 10, 10, 10))
  }

  test("scaleBy") {
    assertEquals(Vector4(2, 2, 10, 20).scaleBy(Vector4(10, 2, 3, 5)), Vector4(20, 4, 30, 100))
  }

  test("ceil") {
    assertEquals(Vector4(2.2, 2.6, 5.1, 7.99999).ceil, Vector4(3, 3, 6, 8))
  }

  test("floor") {
    assertEquals(Vector4(2.2, 2.6, 5.1, 7.99999).floor, Vector4(2, 2, 5, 7))
  }

  test("round") {
    assertEquals(Vector4(2.2, 2.6, 5.1, 7.99999).round, Vector4(2, 3, 5, 8))
  }

  test("normalise") {
    assert(clue(Vector4(10, 10, 10, 10).normalise) ~== clue(Vector4(0.5, 0.5, 0.5, 0.5)))
    assert(clue(Vector4(-10, -10, -10, -10).normalise) ~== clue(Vector4(-0.5, -0.5, -0.5, -0.5)))
    assert(clue(Vector4(10, 0, 0, 0).normalise) == clue(Vector4(1, 0, 0, 0)))
    assert(clue(Vector4(0, 0, 0, 10).normalise) == clue(Vector4(0, 0, 0, 1)))
    assert(clue(Vector4(-50, 1000, 1000, 2000).normalise) ~== clue(Vector4(-0.0204, 0.4081, 0.4081, 0.8163)))
  }

  test("approx equal") {
    assert(Vector4(5.0, 5.0, 5.0, 5.0) ~== Vector4(4.999999, 5.00001, 5.0, 4.9999))
    assert(!(Vector4(5.0, 5.0, 5.0, 5.0) ~== Vector4(-4.999999, 5.00001, 5.0, 5.0)))
  }

  test("mod") {
    assert(Vector4.mod(Vector4(11, 12, 13, 14), Vector4(10)) ~== Vector4(1, 2, 3, 4))
    assert(Vector4(11, 12, 13, 14) % Vector4(10) ~== Vector4(1, 2, 3, 4))
    assert(Vector4.mod(Vector4(9, 10, 11, 12), Vector4(10)) ~== Vector4(9, 0, 1, 2))
    assert(Vector4.mod(Vector4(1), Vector4(10)) ~== Vector4(1))
    assert(Vector4.mod(Vector4(-11, -12, -13, -14), Vector4(10)) ~== Vector4(9, 8, 7, 6))
    assert(Vector4.mod(Vector4(-1), Vector4(10)) ~== Vector4(9))
    assert(Vector4.mod(Vector4(0), Vector4(10)) ~== Vector4(0))
    assert(clue(Vector4.mod(Vector4(-11), Vector4(-10))) ~== clue(Vector4(-1)))
  }

  // ----------------------
  // Vector 4 matrix tests

  test("Multiplying a Vector4 by a Matrix4.should work with identity") {

    val v = Vector4(1, 2, 3, 4)

    val m = Matrix4.identity

    assertEquals(v.transform(m).toVector3, v.toVector3)
    assertEquals(m.transform(v.toVector3), Vector3(1, 2, 3))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(Vector3(5, 10, 20))

    assertEquals(v.transform(m), Vector4.position(15, 20, 30))
    assertEquals(m.transform(v.toVector3), Vector3(15, 20, 30))
  }

  test("Multiplying a Vector4 by a Matrix4.should scale...") {

    val v = Vector4.position(10, 10, 1)

    val m = Matrix4.identity.scale(Vector3(2, 2, 2))

    assertEquals(v.transform(m), Vector4.position(20, 20, 2))
    assertEquals(m.transform(v.toVector3), Vector3(20, 20, 2))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate then scale...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.translate(Vector3(10, 10, 10)).scale(Vector3(2, 2, 2))

    assertEquals(v.transform(m), Vector4.position(40, 40, 40))
    assertEquals(m.transform(v.toVector3), Vector3(40, 40, 40))
  }

  test("Multiplying a Vector4 by a Matrix4.should scale then translate...") {

    val v = Vector4.position(10, 10, 10)

    val m = Matrix4.identity.scale(Vector3(2, 2, 2)).translate(Vector3(10, 10, 10))

    assertEquals(v.transform(m), Vector4.position(30, 30, 30))
    assertEquals(m.transform(v.toVector3), Vector3(30, 30, 30))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate then rotate...1") {

    val v = Vector4.position(0, 0, 0)

    val m = Matrix4.identity
      .translate(Vector3(0, 8, 0))
      .rotate(Radians.TAUby4.negative)

    assert(v.transform(m) ~== Vector4.position(8, 0, 0))
  }

  test("Multiplying a Vector4 by a Matrix4.should translate then rotate...2") {

    val v = Vector4.position(2, 10, 0)

    val m = Matrix4.identity
      .translate(Vector3(-2, -2, 0))
      .rotate(Radians.TAUby4.negative)

    assert(v.transform(m) ~== Vector4.position(8, 0, 0))
  }

  def to2dp(d: Double): Double =
    Math.round(d * 100).toDouble / 100

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

}
