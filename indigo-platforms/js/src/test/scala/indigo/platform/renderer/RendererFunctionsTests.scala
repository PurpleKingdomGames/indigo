package indigo.platform.renderer

import utest._
import indigo.shared.display.DisplayObject
import indigo.shared.datatypes.Vector2
import indigo.shared.display.SpriteSheetFrame

object RendererFunctionsTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should create correct UBO data" - {
        val displayObject: DisplayObject =
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
            2,     // r
            3,     // g
            4,     // b
            false, // flip h
            true,  // flip v
            SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets(Vector2(0.5, 0.5), Vector2(0.1, 0.1))
          )

        /*
          layout (std140) uniform DisplayObjectUBO {
            vec2 u_translation;
            vec2 u_scale;
            vec4 u_tint;
            vec2 u_frameTranslation;
            vec2 u_frameScale;
            float u_rotation;
          };
         */
        val expected: scalajs.js.Array[Double] =
          scalajs.js.Array[Double](
            10,        // x
            10,        // y
            300 * 0.5, // scale x
            200 * 0.5, // scale y
            2,         // r
            3,         // g
            4,         // b
            1,         // a
            0.1,       // frame x
            0.1,       // frame y
            0.5,       // frame scale x
            0.5,       // frame scale y
            2,         // rotation
            0,         //
            0,         //
            0          //
          )

        val actual = RendererFunctions.updateUBOData(displayObject)

        expected.toList ==> actual.toList
      }

    }

}
