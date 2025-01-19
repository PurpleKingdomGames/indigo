package indigo.shared.datatypes

class RGBATests extends munit.FunSuite {

  test("Creating RGBA instances.Should convert from RGBA int values") {
    assertEquals(RGBA.fromColorInts(0, 0, 0, 0), RGBA.Black.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 255, 255, 0), RGBA.White.withAlpha(0))
    assertEquals(RGBA.fromColorInts(255, 0, 0, 255), RGBA.Red)
    assertEquals(RGBA.fromColorInts(0, 255, 0, 255), RGBA.Green)
    assertEquals(RGBA.fromColorInts(0, 0, 255, 255), RGBA.Blue)

    val transparent = RGBA.fromColorInts(255, 255, 255, 127)
    assertEquals(transparent.a > 0.48 && transparent.a < 0.52, true)
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
    assertEquals(RGBA.fromHexString("#FF0000FF"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00FF"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FFFF"), RGBA.Blue)

    assertEquals(RGBA.fromHexString("0xFF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("FF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("#FF0000"), RGBA.Red)
    assertEquals(RGBA.fromHexString("00FF00"), RGBA.Green)
    assertEquals(RGBA.fromHexString("#00FF00"), RGBA.Green)
    assertEquals(RGBA.fromHexString("0000FF"), RGBA.Blue)
    assertEquals(RGBA.fromHexString("#0000FF"), RGBA.Blue)

    val transparent = RGBA.fromHexString("0xFF000080")
    assertEquals(transparent.a > 0.48 && transparent.a < 0.52, true)
  }

  test("Can convert RGBA to Hex") {
    assertEquals(RGBA.Red.toHexString, "ff0000ff")
    assertEquals(RGBA.Green.toHexString, "00ff00ff")
    assertEquals(RGBA.Blue.toHexString, "0000ffff")
    assertEquals(RGBA.Blue.toHexString("#"), "#0000ffff")
  }

  test("mixing colours 50-50 red blue") {
    val colorA = RGBA.Red
    val colorB = RGBA.Blue

    val expected =
      RGBA(0.5, 0.0, 0.5, 1.0)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 50-50 red white") {
    val colorA = RGBA.Red
    val colorB = RGBA.White

    val expected =
      RGBA(1.0, 0.5, 0.5, 1.0)

    val actual =
      colorA.mix(colorB)

    assertEquals(actual, expected)
  }

  test("mixing colours 90-10 red white") {
    val colorA = RGBA.Red
    val colorB = RGBA.White

    val expected =
      RGBA(1.0, 0.1, 0.1, 1.0)

    val actual =
      colorA.mix(colorB, 0.1)

    assertEquals(actual, expected)
  }

}
