package indigo.facades.worker

import indigo.shared.datatypes.Point
import indigo.shared.datatypes.Vector2
import indigo.shared.datatypes.Rectangle

class WorkerConversionsTests extends munit.FunSuite {

  test("point") {
    val expected =
      Point(20, 30)

    val actual =
      PointConversion.fromJS(PointConversion.toJS(expected))

    assertEquals(actual, expected)
  }

  test("vector2") {
    val expected =
      Vector2(20, 30)

    val actual =
      Vector2Conversion.fromJS(Vector2Conversion.toJS(expected))

    assertEquals(actual, expected)
  }

  test("rectangle") {
    val expected =
      Rectangle(Point(20, 30), Point(50, 100))

    val actual =
      RectangleConversion.fromJS(RectangleConversion.toJS(expected))

    assertEquals(actual, expected)
  }

}
