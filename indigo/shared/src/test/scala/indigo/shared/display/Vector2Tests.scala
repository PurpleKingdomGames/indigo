package indigo.shared.display

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

class Vector2Tests extends munit.FunSuite {

  val imageSize     = Vector2(192, 64)
  val frameSize     = Vector2(64, 64)
  val framePosition = Vector2(64, 0)

  test("Basic vector operation.should be able to divide") {
    assertEquals(areDoubleVectorsEqual(Vector2(0.33, 1), frameSize / imageSize), true)
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
        .scale(scaleFactor)
        .translate(multiplier)

    assertEquals(areDoubleVectorsEqual(Vector2(0.66, 1), result), true)

  }

  test("Basic vector operation.should be able to scale and translate") {

    val res = Vector2(10, 10).scale(Vector2(2, 2)).translate(Vector2(5, 5))

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

  def areDoubleVectorsEqual(expected: Vector2, actual: Vector2): Boolean =
    areDoublesEqual(expected.x, actual.x) && areDoublesEqual(expected.y, actual.y)

  def areDoublesEqual(expected: Double, actual: Double): Boolean =
    actual >= expected - 0.01d && actual <= expected + 0.01d

  def to2dp(d: Double): Double =
    Math.round(d * 100).toDouble / 100

}
