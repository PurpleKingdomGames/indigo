package indigo.platform.renderer.shared

class CameraHelperTests extends munit.FunSuite {
  
  test("zoom 0.25") {

    val actual =
      CameraHelper.zoom(0.0d, 0.0d, 100.0d, 50.0d, 0.25d)

    val expected =
      (-150.0d, -75.0d, 400.0d, 200.0d)

    assertEquals(actual, expected)
  }
  
  test("zoom 0.5") {

    val actual =
      CameraHelper.zoom(0.0d, 0.0d, 100.0d, 50.0d, 0.5d)

    val expected =
      (-50.0d, -25.0d, 200.0d, 100.0d)

    assertEquals(actual, expected)
  }
  
  test("zoom x1") {

    val actual =
      CameraHelper.zoom(0.0d, 0.0d, 100.0d, 50.0d, 1.0d)

    val expected =
      (0.0d, 0.0d, 100.0d, 50.0d)

    assertEquals(actual, expected)
  }
  
  test("zoom x2") {

    val actual =
      CameraHelper.zoom(0.0d, 0.0d, 100.0d, 50.0d, 2.0d)

    val expected =
      (25.0d, 12.5d, 50.0d, 25.0d)

    assertEquals(actual, expected)
  }
  
  test("zoom x4") {

    val actual =
      CameraHelper.zoom(0.0d, 0.0d, 100.0d, 50.0d, 4.0d)

    val expected =
      (37.5d, 18.75, 25.0d, 12.5d)

    assertEquals(actual, expected)
  }
  
  test("zoom x2 - off center") {

    val actual =
      CameraHelper.zoom(10.0d, 10.0d, 100.0d, 50.0d, 2.0d)

    val expected =
      (35.0d, 22.5d, 50.0d, 25.0d)

    assertEquals(actual, expected)
  }

}
