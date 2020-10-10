package indigo.shaders

import utest._

object ShaderDsl2Tests extends TestSuite {

  val tests: Tests =
    Tests {

      "test" - {

        /*
         * Take the vertex position and modify it with cos(t)
         */
        val t: Channel = new Channel(() => Float)
        

        1 ==> 2

      }

    }

}
