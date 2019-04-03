package indigo.shared

import indigo.shared.AsString._

import utest._

object AsStringTests extends TestSuite {

  val tests: Tests =
    Tests {

      "should be able to show an int" - {
        10.show ==> "10"
      }

    }

}