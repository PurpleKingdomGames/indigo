package indigo.shared.datatypes

import utest._

object RadiansTests extends TestSuite {

  val tests: Tests =
    Tests {

      "Can make a Radians instance from degrees" - {

        round(Radians.fromDegrees(0)) ==> round(Radians.zero)
        round(Radians.fromDegrees(180)) ==> round(Radians.PI)
        round(Radians.fromDegrees(359)) ==> round(Radians.TAU - Radians(0.02d))
        round(Radians.fromDegrees(360)) ==> round(Radians.zero)

      }

    }

  def round(d: Radians): Double =
    Math.floor(d.value * 100d) / 100d

}
