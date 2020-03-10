package indigo.platform.renderer

import utest._
import indigo.shared.display.DisplayObject
import indigo.shared.datatypes.Vector2
import indigo.shared.display.SpriteSheetFrame
import scalajs.js.JSConverters._
import indigo.shared.datatypes.RGBA
import indigo.shared.display.DisplayEffects

object RendererMergeTests extends TestSuite {

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
            "",    // diffuse
            new SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets(Vector2(0.5, 0.5), Vector2(0.1, 0.1), identity),
            Vector2.minusOne,
            Vector2.minusOne,
            Vector2.minusOne,
            0.0,
            40, // refX
            30, // refY
            DisplayEffects.default
          )

        /*
          layout (std140) uniform DisplayObjectUBO {
            //mat4 u_projection;
            vec2 u_translation;
            vec2 u_scale;
            vec2 u_frameTranslation;
            vec2 u_frameScale;
          };
         */
        val expected: scalajs.js.Array[Double] =
          List.fill(16 * 2)(0.0d).toJSArray

        expected(0) = 10
        expected(1) = 10
        expected(2) = 300 * 0.5
        expected(3) = 200 * 0.5

        expected(4) = 0.1
        expected(5) = 0.1
        expected(6) = 0.5
        expected(7) = 0.5

        expected(8) = 4
        expected(9) = 5
        expected(10) = 6
        expected(11) = 7

        expected(12) = 8
        expected(13) = 9
        expected(14) = 10
        expected(15) = 11

        expected(16) = 12
        expected(17) = 13
        expected(18) = 14
        expected(19) = 15

        expected(20) = 16
        expected(21) = 17
        expected(22) = 18
        expected(23) = 19

        expected(24) = 20
        expected(25) = 21
        expected(26) = 22
        expected(27) = 23

        expected(28) = 1
        expected(29) = 2
        expected(30) = 3
        // expected(31) = 0

        RendererMerge.updateUBOData(
          displayObject,
          RGBA(4, 5, 6, 7),
          RGBA(8, 9, 10, 11),
          RGBA(12, 13, 14, 15),
          RGBA(16, 17, 18, 19),
          RGBA(20, 21, 22, 23),
          1,
          2,
          3
        )

        expected.toList ==> RendererMerge.uboData.toList
      }

    }

}
