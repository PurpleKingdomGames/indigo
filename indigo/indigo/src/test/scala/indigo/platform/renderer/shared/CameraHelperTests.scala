package indigo.platform.renderer.shared

class CameraHelperTests extends munit.FunSuite {

  test("zoom 0.25") {

    val actual =
      CameraHelper.zoom(0.0f, 0.0f, 100.0f, 50.0f, 0.25f)

    val expected =
      (-150.0f, -75.0f, 400.0f, 200.0f)

    assertEquals(actual, expected)
  }

  test("zoom 0.5") {

    val actual =
      CameraHelper.zoom(0.0f, 0.0f, 100.0f, 50.0f, 0.5f)

    val expected =
      (-50.0f, -25.0f, 200.0f, 100.0f)

    assertEquals(actual, expected)
  }

  test("zoom x1") {

    val actual =
      CameraHelper.zoom(0.0f, 0.0f, 100.0f, 50.0f, 1.0f)

    val expected =
      (0.0f, 0.0f, 100.0f, 50.0f)

    assertEquals(actual, expected)
  }

  test("zoom x2") {

    val actual =
      CameraHelper.zoom(0.0f, 0.0f, 100.0f, 50.0f, 2.0f)

    val expected =
      (25.0f, 12.5f, 50.0f, 25.0f)

    assertEquals(actual, expected)
  }

  test("zoom x4") {

    val actual =
      CameraHelper.zoom(0.0f, 0.0f, 100.0f, 50.0f, 4.0f)

    val expected =
      (37.5f, 18.75f, 25.0f, 12.5f)

    assertEquals(actual, expected)
  }

  test("zoom x2 - off center") {

    val actual =
      CameraHelper.zoom(10.0f, 10.0f, 100.0f, 50.0f, 2.0f)

    val expected =
      (35.0f, 22.5f, 50.0f, 25.0f)

    assertEquals(actual, expected)
  }

}
