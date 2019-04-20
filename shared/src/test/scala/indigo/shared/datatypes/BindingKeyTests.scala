package indigo.shared.datatypes

import utest._

import indigo.shared.EqualTo._

object BindingKeyTests extends TestSuite {

  val tests: Tests =
    Tests {
      "Binding keys" - {

        "should generate unique keys on each request" - {

          (BindingKey.generate !== BindingKey.generate) ==> true

        }

        "should be able to generate a key" - {

          "Length should be 16" - {
            BindingKey.generate.value.length ==> 16
          }

          "value should match ^[0-9a-zA-Z]$"- {
            BindingKey.generate.value.matches("""^[0-9a-zA-Z]{16}$""") ==> true
          }

        }

      }
    }

}
