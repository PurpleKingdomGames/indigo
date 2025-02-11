package indigo.shared.scenegraph

import indigo.shared.AnimationsRegister
import indigo.shared.BoundaryLocator
import indigo.shared.FontRegister
import indigo.shared.assets.AssetName
import indigo.shared.collections.Batch
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Rectangle
import indigo.shared.materials.Material

class TextTests extends munit.FunSuite {

  val material = Material.Bitmap(AssetName("font-sheet"))

  val fontRegister: FontRegister =
    new FontRegister

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  test("Text entities should be able to correctly calculate the bounds where all are equal") {

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, fontKey, material)

    assertEquals(
      boundaryLocator.findBounds(t).get,
      Rectangle(10, 20, 16 * 3, 16)
    )

    fontRegister.clearRegister()
  }

  test("Text entities should be able to correctly calculate the bounds with different sized chars") {

    val chars = Batch(
      FontChar("a", 0, 16, 10, 10),
      FontChar("b", 30, 16, 20, 20),
      FontChar("c", 60, 16, 30, 30)
    )

    val fontKey = FontKey("test2")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, fontKey, material)

    val actual   = boundaryLocator.findBounds(t).get   // 48 x 16
    val expected = Rectangle(10, 20, 10 + 20 + 30, 30) // 60 x 30

    assertEquals(actual, expected)

    fontRegister.clearRegister()
  }

  test("Text entities should be able to correctly calculate the bounds where all are equal (align center)") {

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, fontKey, material).alignCenter

    val width = 16 * 3

    val actual   = boundaryLocator.findBounds(t).get
    val expected = Rectangle(10 - (width / 2), 20, width, 16)

    assertEquals(actual, expected)

    fontRegister.clearRegister()
  }

  test("Text entities should be able to correctly calculate the bounds where all are equal (align right)") {

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, fontKey, material).alignRight

    val width = 16 * 3

    val actual   = boundaryLocator.findBounds(t).get
    val expected = Rectangle(10 - width, 20, width, 16)

    assertEquals(actual, expected)
    assertEquals(actual, boundaryLocator.findBounds(t).get)

    fontRegister.clearRegister()
  }

  test("Text entities should be able to correctly calculate the bounds where all are equal (align right, rotated)") {

    val chars = Batch(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 0, 0, fontKey, material).alignRight
      .rotateTo(Radians.TAUby2)

    val width = 16 * 3

    val actual   = boundaryLocator.findBounds(t).get
    val expected = Rectangle(0, -16, width, 16)

    assertEquals(actual, expected)
    assertEquals(actual, boundaryLocator.findBounds(t).get)

    fontRegister.clearRegister()
  }

}
