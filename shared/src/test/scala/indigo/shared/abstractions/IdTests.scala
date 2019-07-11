package shared.abstractions

import utest._

import indigo.shared.EqualTo._

object IdTests extends TestSuite {

  val tests: Tests =
    Tests {
      "The Id Monad" - {

        "should be constructable" - {
          Id(12) === Id(12) ==> true
          Id(4) === Id(4) ==> true
          Id.pure("hello") === Id("hello") ==> true
        }

        "should be mappable" - {
          Id(4).map(_ * 5) === Id(20) ==> true
        }

        "should be flattenable" - {
          Id.flatten(Id(Id(4))) === Id(4) ==> true
        }

        "should be flatMappable" - {
          Id(3).flatMap(i => Id(i + i)) === Id(6) ==> true
        }

      }
    }
}
