package indigo.shared.datatypes

import utest._

object RGBATests extends TestSuite {

  val tests: Tests =
    Tests {
      "Creating RGBA instances" - {

        "Should convert from RGBA int values" - {
          RGBA.fromColorInts(0, 0, 0, 0) ==> RGBA.Black.withAlpha(0)
          RGBA.fromColorInts(255, 255, 255, 0) ==> RGBA.White.withAlpha(0)
          RGBA.fromColorInts(255, 0, 0, 255) ==> RGBA.Red
          RGBA.fromColorInts(0, 255, 0, 255) ==> RGBA.Green
          RGBA.fromColorInts(0, 0, 255, 255) ==> RGBA.Blue

          val transparent = RGBA.fromColorInts(255, 255, 255, 127)
          (transparent.a > 0.48 && transparent.a < 0.52) ==> true
        }

        "should convert from RGB int values" - {
          RGBA.fromColorInts(0, 0, 0) ==> RGBA.Black
          RGBA.fromColorInts(255, 255, 255) ==> RGBA.White
          RGBA.fromColorInts(255, 0, 0) ==> RGBA.Red
          RGBA.fromColorInts(0, 255, 0) ==> RGBA.Green
          RGBA.fromColorInts(0, 0, 255) ==> RGBA.Blue
        }

        "should convert from Hexadecimal" - {
          RGBA.fromHexString("0xFF0000FF") ==> RGBA.Red
          RGBA.fromHexString("FF0000FF") ==> RGBA.Red
          RGBA.fromHexString("00FF00FF") ==> RGBA.Green
          RGBA.fromHexString("0000FFFF") ==> RGBA.Blue

          RGBA.fromHexString("0xFF0000") ==> RGBA.Red
          RGBA.fromHexString("FF0000") ==> RGBA.Red
          RGBA.fromHexString("00FF00") ==> RGBA.Green
          RGBA.fromHexString("0000FF") ==> RGBA.Blue

          val transparent = RGBA.fromHexString("0xFF000080") 
          (transparent.a > 0.48 && transparent.a < 0.52) ==> true
        }

      }
    }

}
