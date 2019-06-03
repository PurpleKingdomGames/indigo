package indigo.shared.display

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

import utest._

object Vector2Tests extends TestSuite {

  val imageSize     = Vector2(192, 64)
  val frameSize     = Vector2(64, 64)
  val framePosition = Vector2(64, 0)

  val tests: Tests =
    Tests {
      "Basic vector operation" - {

        "should be able to divide" - {
          areDoubleVectorsEqual(Vector2(0.33, 1), frameSize / imageSize) ==> true
        }

        "should be able to find a frame" - {

          val scaleFactor = frameSize / imageSize

          val frameOffsetFactor = framePosition / frameSize
          val multiplier        = scaleFactor * frameOffsetFactor

          val result =
            Vector2(1, 1)
              .scale(scaleFactor)
              .translate(multiplier)

          areDoubleVectorsEqual(Vector2(0.66, 1), result) ==> true

        }

        "should be able to scale and translate" - {

          val res = Vector2(10, 10).scale(Vector2(2, 2)).translate(Vector2(5, 5))

          res.x ==> 25
          res.y ==> 25

        }

        "should be able to calculate the dot product between two Vector2s" - {

          "parallel" - {
            (Vector2(0, 0) dot Vector2(0, 0)) ==> 0f
          }

          "facing" - {
            (Vector2(2, 2) dot Vector2(-1, -1)) < 0 ==> true
          }

          "not facing" - {
            (Vector2(2, 2) dot Vector2(1, 1)) > 0 ==> true
          }

          "value" - {
            Math.round((Vector2(-6, 8) dot Vector2(5, 12))) ==> 66
          }

        }

      }

      "Construction" - {

        "build a vector from two points" - {
          Vector2.fromPoints(Point.zero, Point(2, 2)) === Vector2(2, 2) ==> true
          Vector2.fromPoints(Point(10, 2), Point(2, 2)) === Vector2(-8, 0) ==> true
        }

      }
    }

  def areDoubleVectorsEqual(expected: Vector2, actual: Vector2): Boolean =
    areDoublesEqual(expected.x, actual.x) && areDoublesEqual(expected.y, actual.y)

  def areDoublesEqual(expected: Double, actual: Double): Boolean =
    actual >= expected - 0.01d && actual <= expected + 0.01d

}
