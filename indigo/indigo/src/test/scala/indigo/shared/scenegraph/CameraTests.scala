package indigo.shared.scenegraph

import indigo.Point
import indigo.Rectangle
import indigo.Size

class CameraTests extends munit.FunSuite {

  test("Camera.LookAt can return the top left corner of the frustrum") {

    val actual =
      Camera.LookAt(Point(100)).topLeft(Size(80, 40))

    val expected =
      Point(100 - 40, 100 - 20)

    assertEquals(actual, expected)
  }

  test("Camera.Fixed bounds") {

    val actual =
      Camera.Fixed(Point(100)).bounds(Size(80, 40))

    val expected =
      Rectangle(100, 100, 80, 40)

    assertEquals(actual, expected)
  }

  test("Camera.LookAt bounds") {

    val actual =
      Camera.LookAt(Point(100)).frustum(Size(80, 40))

    val expected =
      Rectangle(100 - 40, 100 - 20, 80, 40)

    assertEquals(actual, expected)
  }

}
