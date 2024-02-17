package indigoplugin.generators

class ConfigGenTests extends munit.FunSuite {

  test("Can detect/extract rgba") {

    val actual =
      ConfigGen.extractBgColor("rgba(255, 127, 12,   255)")

    val expected =
      "RGBA.fromColorInts(255, 127, 12, 255)"

    assertEquals(actual, expected)
  }

  test("Can detect/extract rgb") {

    val actual =
      ConfigGen.extractBgColor("rgb(  255, 127, 12)")

    val expected =
      "RGBA.fromColorInts(255, 127, 12)"

    assertEquals(actual, expected)
  }

  test("Can detect/extract hex") {

    val actual =
      ConfigGen.extractBgColor("#FFFF00")

    val expected =
      """RGBA.fromHexString("#FFFF00")"""

    assertEquals(actual, expected)
  }

  test("Can detect/extract mixed case hex") {

    val actual =
      ConfigGen.extractBgColor("#FffF00")

    val expected =
      """RGBA.fromHexString("#FffF00")"""

    assertEquals(actual, expected)
  }

  test("Can detect/extract named colour") {

    val actual =
      ConfigGen.extractBgColor("red")

    val expected =
      """RGBA.fromHexString("#FF0000")"""

    assertEquals(actual, expected)
  }

  test("Will fall back to black") {

    val actual =
      ConfigGen.extractBgColor("nonsense")

    val expected =
      "RGBA.Black"

    assertEquals(actual, expected)
  }

}
