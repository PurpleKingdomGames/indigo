package indigo.shared.datatypes

import utest._
import indigo.shared.time.Seconds

object RadiansTests extends TestSuite {

  val tests: Tests =
    Tests {

      "constants are equivalent" - {
        Radians.TAU ==> Radians.`2PI`
        Radians.TAUby2 ==> Radians.PI
        Radians.TAUby4 ==> Radians.PIby2
      }

      "Can make a Radians instance from degrees" - {

        round(Radians.fromDegrees(0)) ==> round(Radians.zero)
        round(Radians.fromDegrees(180)) ==> round(Radians.PI)
        round(Radians.fromDegrees(359)) ==> round(Radians.TAU - Radians(0.02d))
        round(Radians.fromDegrees(360)) ==> round(Radians.zero)

      }

      "Can convert seconds to Radians" - {
        round(Radians.fromSeconds(Seconds(0))) ==> round(Radians.zero)
        round(Radians.fromSeconds(Seconds(0.5))) ==> round(Radians.PI)
        round(Radians.fromSeconds(Seconds(1))) ==> round(Radians.zero)
        round(Radians.fromSeconds(Seconds(1.5))) ==> round(Radians.PI)
      }

    }

  def round(d: Radians): Double =
    Math.floor(d.value * 100d) / 100d

}
