package indigo.shared.datatypes

class RGBTests extends munit.FunSuite {

  test("Creating RGB instances.should convert from RGB int values") {
    assertEquals(RGB.fromColorInts(0, 0, 0), RGB.Black)
    assertEquals(RGB.fromColorInts(255, 255, 255), RGB.White)
    assertEquals(RGB.fromColorInts(255, 0, 0), RGB.Red)
    assertEquals(RGB.fromColorInts(0, 255, 0), RGB.Green)
    assertEquals(RGB.fromColorInts(0, 0, 255), RGB.Blue)
  }

  test("Creating RGB instances.should convert from Hexadecimal") {
    assertEquals(RGB.fromHexString("0xFF0000"), RGB.Red)
    assertEquals(RGB.fromHexString("FF0000"), RGB.Red)
    assertEquals(RGB.fromHexString("00FF00"), RGB.Green)
    assertEquals(RGB.fromHexString("0000FF"), RGB.Blue)

    assertEquals(RGB.fromHexString("0xFF0000FF"), RGB.Red)
  }

  test("mixing colours 50-50 red blue") {
    val colorA = RGB.Red
    val colorB = RGB.Blue

    val expected =
      RGB(0.5, 0.0, 0.5)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 50-50 red white") {
    val colorA = RGB.Red
    val colorB = RGB.White

    val expected =
      RGB(1.0, 0.5, 0.5)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 90-10 red white") {
    val colorA = RGB.Red
    val colorB = RGB.White

    val expected =
      RGB(1.0, 0.1, 0.1)

    val actual =
      colorA.mix(colorB, 0.1)

    assertEquals(actual, expected)
  }

}
