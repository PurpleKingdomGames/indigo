package indigo.shared.datatypes

import utest._

object RGBTests extends TestSuite {

  val tests: Tests =
    Tests {
      "Creating RGB instances" - {

        "should convert from RGB int values" - {
          RGB.fromColorInts(0, 0, 0) ==> RGB.Black
          RGB.fromColorInts(255, 255, 255) ==> RGB.White
          RGB.fromColorInts(255, 0, 0) ==> RGB.Red
          RGB.fromColorInts(0, 255, 0) ==> RGB.Green
          RGB.fromColorInts(0, 0, 255) ==> RGB.Blue
        }

        "should convert from Hexadecimal" - {
          RGB.fromHexString("0xFF0000") ==> RGB.Red
          RGB.fromHexString("FF0000") ==> RGB.Red
          RGB.fromHexString("00FF00") ==> RGB.Green
          RGB.fromHexString("0000FF") ==> RGB.Blue

          RGB.fromHexString("0xFF0000FF") ==> RGB.Red
        }

      }
    }

}
