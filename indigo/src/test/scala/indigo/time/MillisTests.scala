package indigo.time

import utest._

object MillisTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should be able to convert Millis to Seconds" - {
        Millis(1000).toSeconds ==> Seconds(1)
        Millis(1500).toSeconds ==> Seconds(1.5)
        Millis(10001).toSeconds ==> Seconds(10.001)
      }

    }
}