package indigo.shared

import utest._

object ClearColorTests extends TestSuite {

  val tests: Tests =
    Tests {
      "Creating clear color instances" - {

        "Should convert from RGBA" - {
          ClearColor.fromRGBA(0, 0, 0, 0) ==> ClearColor.Black.withA(0)
          ClearColor.fromRGBA(255, 255, 255, 0) ==> ClearColor.White.withA(0)
          ClearColor.fromRGBA(255, 0, 0, 255) ==> ClearColor.Red
          ClearColor.fromRGBA(0, 255, 0, 255) ==> ClearColor.Green
          ClearColor.fromRGBA(0, 0, 255, 255) ==> ClearColor.Blue

          val transparent = ClearColor.fromRGBA(255, 255, 255, 127)
          (transparent.a > 0.48 && transparent.a < 0.52) ==> true
        }

        "should convert from RGB" - {
          ClearColor.fromRGB(0, 0, 0) ==> ClearColor.Black
          ClearColor.fromRGB(255, 255, 255) ==> ClearColor.White
          ClearColor.fromRGB(255, 0, 0) ==> ClearColor.Red
          ClearColor.fromRGB(0, 255, 0) ==> ClearColor.Green
          ClearColor.fromRGB(0, 0, 255) ==> ClearColor.Blue
        }

        "should convert from Hexadecimal" - {
          println(ClearColor.fromHexString("0xFF0000"))
          println(ClearColor.Red)

          ClearColor.fromHexString("0xFF0000") ==> ClearColor.Red
          ClearColor.fromHexString("FF0000") ==> ClearColor.Red
          ClearColor.fromHexString("00FF00") ==> ClearColor.Green
          ClearColor.fromHexString("0000FF") ==> ClearColor.Blue
        }

      }
    }

}
