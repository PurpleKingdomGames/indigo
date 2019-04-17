package indigo.time

import utest._

import indigo.EqualTo._

object MillisTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should be able to convert Millis to Seconds" - {
        Millis(1000).toSeconds ==> Seconds(1)
        Millis(1500).toSeconds ==> Seconds(1.5)
        Millis(10001).toSeconds ==> Seconds(10.001)
      }

      "Operations" - {

        "modulo" - {

          Millis(1) % Millis(2) === Millis(1) ==> true
          Millis(2) % Millis(2) === Millis(0) ==> true

        }

      }

    }
}