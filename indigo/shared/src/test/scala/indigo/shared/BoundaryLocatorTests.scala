package indigo.shared

import utest._
import indigo.shared.datatypes.FontChar
import indigo.shared.datatypes.FontKey
import indigo.shared.datatypes.FontInfo
import indigo.shared.datatypes.Material
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.Rectangle
import indigo.shared.scenegraph.Text

object BoundaryLocatorTests extends TestSuite {

  import Samples._

  val fontRegister: FontRegister =
    new FontRegister

  fontRegister.register(fontInfo)

  val boundaryLocator: BoundaryLocator =
    new BoundaryLocator(new AnimationsRegister, fontRegister)

  val tests: Tests =
    Tests {

      "Text boundary calculations" - {

        "Text as lines with bounds" - {
          "empty" - {
            val actual = boundaryLocator.textAsLinesWithBounds("", fontKey)
            actual.length ==> 0
          }
          "abc" - {
            val actual = boundaryLocator.textAsLinesWithBounds("abc", fontKey)
            actual.length ==> 1
            actual.headOption.get.text ==> "abc"
            actual.headOption.get.lineBounds ==> Rectangle(0, 0, 42, 20)
          }
          "ab->c" - {
            val actual = boundaryLocator.textAsLinesWithBounds("ab\nc", fontKey)
            actual.length ==> 2
            actual(0).text ==> "ab"
            actual(0).lineBounds ==> Rectangle(0, 0, 26, 20)
            actual(1).text ==> "c"
            actual(1).lineBounds ==> Rectangle(0, 20, 16, 16)
          }

        }

        "Text bounds" - {

          // These should be identical, regardless of alignment.
          // The lines move around within the same bounding area.
          "aligned left" - {
            "empty" - {
              val actual = boundaryLocator.textBounds(text.alignLeft.withText(""))
              actual ==> Rectangle(50, 50, 0, 0)
            }
            "abc" - {
              val actual = boundaryLocator.textBounds(text.alignLeft.withText("abc"))
              actual ==> Rectangle(50, 50, 42, 20)
            }
            "ab->c" - {
              val actual = boundaryLocator.textBounds(text.alignLeft.withText("ab\nc"))
              actual ==> Rectangle(50, 50, 26, 36)
            }
          }
          "aligned right" - {
            "empty" - {
              val actual = boundaryLocator.textBounds(text.alignRight.withText(""))
              actual ==> Rectangle(50, 50, 0, 0)
            }
            "abc" - {
              val actual = boundaryLocator.textBounds(text.alignRight.withText("abc"))
              actual ==> Rectangle(50, 50, 42, 20)
            }
            "ab->c" - {
              val actual = boundaryLocator.textBounds(text.alignRight.withText("ab\nc"))
              actual ==> Rectangle(50, 50, 26, 36)
            }
          }
          "aligned center" - {
            "empty" - {
              val actual = boundaryLocator.textBounds(text.alignCenter.withText(""))
              actual ==> Rectangle(50, 50, 0, 0)
            }
            "abc" - {
              val actual = boundaryLocator.textBounds(text.alignCenter.withText("abc"))
              actual ==> Rectangle(50, 50, 42, 20)
            }
            "ab->c" - {
              val actual = boundaryLocator.textBounds(text.alignCenter.withText("ab\nc"))
              actual ==> Rectangle(50, 50, 26, 36)
            }
          }

        }

      }

    }

  object Samples {
    val material = Material.Textured(AssetName("font-sheet"))

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
