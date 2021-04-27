package indigo.shared.scenegraph

import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2

class LightTests extends munit.FunSuite {

  val spot = SpotLight.default

  val tau = Radians.TAU.toDouble

  test("The lookDirection(..) method returns the same SpotLight as a manual rotation") {
    // 0
    assertEquals(spot.rotateTo(Radians((tau / 4) * 0)).rotation, spot.lookDirection(Vector2(1, 0)).rotation)
    // 90
    assertEquals(spot.rotateTo(Radians((tau / 4) * 1)).rotation, spot.lookDirection(Vector2(0, 1)).rotation)
    // 180
    assertEquals(spot.rotateTo(Radians((tau / 4) * 2)).rotation, spot.lookDirection(Vector2(-1, 0)).rotation)
    // 270
    assertEquals(spot.rotateTo(Radians((tau / 4) * 3)).rotation, spot.lookDirection(Vector2(0, -1)).rotation)
  }

  test("The lookAt(..) method returns the same SpotLight as a manual rotation") {
    // 0
    assertEquals(spot.rotateTo(Radians((tau / 4) * 0)).rotation, spot.lookAt(Point(100, 0)).rotation)
    // 90
    assertEquals(spot.rotateTo(Radians((tau / 4) * 1)).rotation, spot.lookAt(Point(0, 100)).rotation)
    // 180
    assertEquals(spot.rotateTo(Radians((tau / 4) * 2)).rotation, spot.lookAt(Point(-100, 0)).rotation)
    // 270
    assertEquals(spot.rotateTo(Radians((tau / 4) * 3)).rotation, spot.lookAt(Point(0, -100)).rotation)
  }

}
