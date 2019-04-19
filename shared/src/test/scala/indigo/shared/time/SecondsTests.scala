package indigo.shared.time

import utest._

object SecondsTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Should be able to convert Seconds to Millis" - {

        Seconds(10).toMillis ==> Millis(10000)
        Seconds(1.5).toMillis ==> Millis(1500)
        Seconds(1).toMillis ==> Millis(1000)
        
      }

    }

}