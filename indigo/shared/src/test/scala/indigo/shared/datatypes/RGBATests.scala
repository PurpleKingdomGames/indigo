package indigo.shared.datatypes

class RGBATests extends munit.FunSuite {

  test("Creating RGBA instances.Should convert from RGBA int values") {
    assertEquals(RGBA.fromColorInts(0, 0, 0, 0), RGBA.Black.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 255, 255, 0), RGBA.White.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 0, 0, 255), RGBA.Red)
    assertEquals(RGBA.fromColorInts(0, 255, 0, 255), RGBA.Green)
    assertEquals(RGBA.fromColorInts(0, 0, 255, 255), RGBA.Blue)

    val transparent = RGBA.fromColorInts(255, 255, 255, 127)
    assertEquals((transparent.a > 0.48 && transparent.a < 0.52), true)
  }

  test("Creating RGBA instances.should convert from RGB int values") {
    assertEquals(RGBA.fromColorInts(0, 0, 0), RGBA.Black)
    assertEquals(RGBA.fromColorInts(255, 255, 255), RGBA.White)
    assertEquals(RGBA.fromColorInts(255, 0, 0), RGBA.Red)
    assertEquals(RGBA.fromColorInts(0, 255, 0), RGBA.Green)
    assertEquals(RGBA.fromColorInts(0, 0, 255), RGBA.Blue)
  }

  test("Creating RGBA instances.should convert from Hexadecimal") {
    assertEquals(RGBA.fromHexString("0xFF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("FF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00FF"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FFFF"), RGBA.Blue)

    assertEquals(RGBA.fromHexString("0xFF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("FF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FF"), RGBA.Blue)

    val transparent = RGBA.fromHexString("0xFF000080")
    assertEquals((transparent.a > 0.48 && transparent.a < 0.52), true)
  }

}
