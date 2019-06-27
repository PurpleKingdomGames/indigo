package indigo.platform.renderer

import utest._
import indigo.shared.display.DisplayObject
import indigo.shared.datatypes.Vector2
import indigo.shared.display.SpriteSheetFrame

object RendererFunctionsTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should create correct UBO data" - {
        val displyObject: DisplayObject =
          DisplayObject(
            10,    // x
            10,    // y
            5,     // z
            300,   // width
            200,   // height
            2,     // rotation (radians)
            0.5,   // scale X
            0.5,   // scale Y
            "",    // imageref
            1,     // a
            1,     // r
            1,     // g
            1,     // b
            false, //flip h
            false, // flip v
            SpriteSheetFrame.defaultOffset
          )

        val expected: scalajs.js.Array[Double] =
          scalajs.js.Array[Double](
            10,        // x
            10,        // y
            300 * 0.5, // scale x
            200 * 0.5, // scale y
            2,         // rotation
            0,
            0,
            0
          )

        val actual = RendererFunctions.makeUBOData(displyObject)

        expected.toList ==> actual.toList
      }

    }

}
