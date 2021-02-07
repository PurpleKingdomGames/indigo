package indigo.shared.scenegraph

import indigo.shared.FontRegister
import indigo.shared.datatypes.{FontChar, FontInfo, FontKey, Rectangle}

import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Material
import indigo.shared.BoundaryLocator
import indigo.shared.AnimationsRegister

class TextTests extends munit.FunSuite {

  val material = Material.Basic(AssetName("font-sheet"))

  val fontRegister: FontRegister =
    new FontRegister

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  test("Text entities should be able to correctly calculate the bounds where all are equal") {

    val chars = List(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 16, 16),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("test1")

    val fontInfo = FontInfo(fontKey, material, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, 1, fontKey)

    assertEquals(t.bounds(boundaryLocator) === Rectangle(10, 20, 16 * 3, 16), true)

    fontRegister.clearRegister()
  }

  test("Text entities should be able to correctly calculate the bounds with different sized chars") {

    val chars = List(
      FontChar("a", 0, 16, 10, 10),
      FontChar("b", 30, 16, 20, 20),
      FontChar("c", 60, 16, 30, 30)
    )

    val fontKey = FontKey("test2")

    val fontInfo = FontInfo(fontKey, material, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    fontRegister.register(fontInfo)

    val t = Text("abc", 10, 20, 1, fontKey)

    val actual   = t.bounds(boundaryLocator)           // 48 x 16
    val expected = Rectangle(10, 20, 10 + 20 + 30, 30) // 60 x 30

    assertEquals(actual === expected, true)

    fontRegister.clearRegister()
  }

}
