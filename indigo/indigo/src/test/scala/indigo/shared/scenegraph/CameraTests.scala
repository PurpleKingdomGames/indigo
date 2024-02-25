package indigo.shared.scenegraph

import indigo.Point
import indigo.Size
import indigo.shared.config.GameViewport

class CameraTests extends munit.FunSuite {
  
  test("Camera.LookAt can return the top left corner of the frustrum") {

    val actual =
      Camera.LookAt(Point(100)).topLeft(Size(80, 40))

    val expected =
      Point(100 - 40, 100 - 20)

    assertEquals(actual, expected)
  }

}
