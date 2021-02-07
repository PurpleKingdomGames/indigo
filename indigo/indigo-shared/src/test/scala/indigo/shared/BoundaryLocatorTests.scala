package indigo.shared

import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontInfo
import indigo.shared.materials.StandardMaterial
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Text

class BoundaryLocatorTests extends munit.FunSuite {

  import Samples._

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  test("Text boundary calculations.Text as lines with bounds.empty") {
    val actual = boundaryLocator.textAsLinesWithBounds("", fontKey)
    assertEquals(actual.length, 0)
  }
  test("Text boundary calculations.Text as lines with bounds.abc") {
    val actual = boundaryLocator.textAsLinesWithBounds("abc", fontKey)
    assertEquals(actual.length, 1)
    assertEquals(actual.headOption.get.text, "abc")
    assertEquals(actual.headOption.get.lineBounds, Rectangle(0, 0, 42, 20))
  }
  test("Text boundary calculations.Text as lines with bounds.ab->c") {
    val actual = boundaryLocator.textAsLinesWithBounds("ab\nc", fontKey)
    assertEquals(actual.length, 2)
    assertEquals(actual(0).text, "ab")
    assertEquals(actual(0).lineBounds, Rectangle(0, 0, 26, 20))
    assertEquals(actual(1).text, "c")
    assertEquals(actual(1).lineBounds, Rectangle(0, 20, 16, 16))
  }

  // These should be identical, regardless of alignment.
  // The lines move around within the same bounding area.
  test("Text boundary calculations.Text bounds.aligned left.empty") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText(""))
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned left.abc") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("abc"))
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned left.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignLeft.withText("ab\nc"))
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  test("Text boundary calculations.Text bounds.aligned right.empty") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText(""))
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned right.abc") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText("abc"))
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned right.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignRight.withText("ab\nc"))
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  test("Text boundary calculations.Text bounds.aligned center.empty") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText(""))
    assertEquals(actual, Rectangle(50, 50, 0, 0))
  }
  test("Text boundary calculations.Text bounds.aligned center.abc") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText("abc"))
    assertEquals(actual, Rectangle(50, 50, 42, 20))
  }
  test("Text boundary calculations.Text bounds.aligned center.ab->c") {
    val actual = boundaryLocator.textBounds(text.alignCenter.withText("ab\nc"))
    assertEquals(actual, Rectangle(50, 50, 26, 36))
  }

  object Samples {
    val material = Material.Basic(AssetName("font-sheet"))

    val chars = List(
      FontChar("a", 0, 16, 16, 16),
      FontChar("b", 16, 16, 10, 20),
      FontChar("c", 32, 16, 16, 16)
    )

    val fontKey = FontKey("boundary locator tests")

    val fontInfo = FontInfo(fontKey, material, 256, 256, FontChar("?", 0, 0, 16, 16)).addChars(chars)

    val text: Text =
      Text("<test>", 50, 50, 1, fontKey)
  }

}
