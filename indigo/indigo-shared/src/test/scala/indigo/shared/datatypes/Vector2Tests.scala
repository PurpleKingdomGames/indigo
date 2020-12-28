package indigo.shared.datatypes

class Vector2Tests extends munit.FunSuite {

  val imageSize     = Vector2(192, 64)
  val frameSize     = Vector2(64, 64)
  val framePosition = Vector2(64, 0)

  test("Basic vector operation.should be able to divide") {
    assert(clue(Vector2(0.3333, 1)) ~== clue(frameSize / imageSize))
  }

  test("Basic vector operation.distance function") {
    assertEquals(to2dp(Vector2.distance(Vector2(1, 2), Vector2(7, 6))), 7.21)
    assertEquals(to2dp(Vector2.distance(Vector2(-6, 8), Vector2(-3, 9))), 3.16)
  }

  test("Basic vector operation.should be able to find a frame") {

    val scaleFactor = frameSize / imageSize

    val frameOffsetFactor = framePosition / frameSize
    val multiplier        = scaleFactor * frameOffsetFactor

    val result =
      Vector2(1, 1)
        .scaleBy(scaleFactor)
        .translate(multiplier)

    assert(Vector2(0.66666, 1) ~== result)

  }

  test("Basic vector operation.should be able to scale and translate") {

    val res = Vector2(10, 10).scaleBy(Vector2(2, 2)).translate(Vector2(5, 5))

    assertEquals(res.x, 25.0)
    assertEquals(res.y, 25.0)

  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector2s.parallel") {
    assertEquals((Vector2(0, 0) dot Vector2(0, 0)), 0.0)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector2s.facing") {
    assertEquals((Vector2(2, 2) dot Vector2(-1, -1)) < 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector2s.not facing") {
    assertEquals((Vector2(2, 2) dot Vector2(1, 1)) > 0, true)
  }

  test("Basic vector operation.should be able to calculate the dot product between two Vector2s.value") {
    assertEquals(Math.round((Vector2(-6.0, 8.0) dot Vector2(5.0, 12.0))).toDouble, 66.0d)
  }

  test("Construction.build a vector from two points") {
    assertEquals(Vector2.fromPoints(Point.zero, Point(2, 2)) === Vector2(2, 2), true)
    assertEquals(Vector2.fromPoints(Point(10, 2), Point(2, 2)) === Vector2(-8, 0), true)
  }

  test("dot product") {
    assert(clue(Vector2(1, 0).dot(Vector2(1, 0))) == 1)
    assert(clue(Vector2(1, 0).dot(Vector2(0, -1))) == 0)
    assert(clue(Vector2(-1, 0).dot(Vector2(1, 0))) == -1)
    assert(clue(Vector2(1, 0).dot(Vector2(-1, 0))) == -1)
  }

  test("abs") {
    assertEquals(Vector2(1, 1).abs, Vector2(1, 1))
    assertEquals(Vector2(-1, 1).abs, Vector2(1, 1))
    assertEquals(Vector2(1, -1).abs, Vector2(1, 1))
    assertEquals(Vector2(-1, -1).abs, Vector2(1, 1))
  }

  test("min") {
    assertEquals(Vector2(10, 10).min(1), Vector2(1, 1))
    assertEquals(Vector2(10, 10).min(100), Vector2(10, 10))
    assertEquals(Vector2(10, 10).min(Vector2(50, 5)), Vector2(10, 5))
  }

  test("max") {
    assertEquals(Vector2(10, 10).max(1), Vector2(10, 10))
    assertEquals(Vector2(10, 10).max(100), Vector2(100, 100))
    assertEquals(Vector2(10, 10).max(Vector2(50, 5)), Vector2(50, 10))
  }

  test("clamp") {
    assertEquals(Vector2(0.1, 0.1).clamp(0, 1), Vector2(0.1, 0.1))
    assertEquals(Vector2(-0.1, 1.1).clamp(0, 1), Vector2(0.0, 1.0))
    assertEquals(Vector2(1, 4).clamp(2, 3), Vector2(2, 3))
  }

  test("length") {
    assertEquals(Vector2(10, 0).length, 10.0)
    assertEquals(Vector2(0, 10).length, 10.0)
    assert(nearEnoughEqual(Vector2(10, 10).length, 14.14d, 0.01))
  }

  test("invert") {
    assertEquals(Vector2(1, 1).invert, Vector2(-1, -1))
    assertEquals(Vector2(-1, 1).invert, Vector2(1, -1))
    assertEquals(Vector2(1, -1).invert, Vector2(-1, 1))
    assertEquals(Vector2(-1, -1).invert, Vector2(1, 1))
  }

  test("translate | moveBy | moveTo") {
    assertEquals(Vector2(1, 1).translate(Vector2(10, 10)), Vector2(11, 11))
    assertEquals(Vector2(1, 1).moveBy(Vector2(10, 10)), Vector2(11, 11))
    assertEquals(Vector2(1, 1).moveTo(Vector2(10, 10)), Vector2(10, 10))
  }

  test("scaleBy") {
    assertEquals(Vector2(2, 2).scaleBy(Vector2(10, 2)), Vector2(20, 4))
  }

  test("round") {
    assertEquals(Vector2(2.2, 2.6).round, Vector2(2, 3))
  }

  test("normalise") {
    assert(clue(Vector2(10, 10).normalise) === clue(Vector2(1, 1)))
    assert(clue(Vector2(-10, -10).normalise) === clue(Vector2(-1, -1)))
    assert(clue(Vector2(10, 0).normalise) === clue(Vector2(1, 0)))
    assert(clue(Vector2(0, 10).normalise) === clue(Vector2(0, 1)))
    assert(clue(Vector2(-50, 1000).normalise) === clue(Vector2(-1, 1)))
  }

  test("approx equal") {
    assert(Vector2(5.0, 5.0) ~== Vector2(4.999999, 5.00001))
    assert(!(Vector2(5.0, 5.0) ~== Vector2(-4.999999, 5.00001)))
  }

  def to2dp(d: Double): Double =
    Math.round(d * 100).toDouble / 100

  def nearEnoughEqual(d1: Double, d2: Double, tolerance: Double): Boolean =
    d1 >= d2 - tolerance && d1 <= d2 + tolerance

}
