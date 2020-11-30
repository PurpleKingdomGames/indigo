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

}
