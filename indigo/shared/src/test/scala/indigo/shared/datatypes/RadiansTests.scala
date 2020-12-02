package indigo.shared.datatypes

import indigo.shared.time.Seconds

class RadiansTests extends munit.FunSuite {

  test("constants are equivalent") {
    assertEquals(Radians.TAU, Radians.`2PI`)
    assertEquals(Radians.TAUby2, Radians.PI)
    assertEquals(Radians.TAUby4, Radians.PIby2)
  }

  test("Can make a Radians instance from degrees") {

    assertEquals(round(Radians.fromDegrees(0)), round(Radians.zero))
    assertEquals(round(Radians.fromDegrees(180)), round(Radians.PI))
    assertEquals(round(Radians.fromDegrees(359)), round(Radians.TAU - Radians(0.02d)))
    assertEquals(round(Radians.fromDegrees(360)), round(Radians.zero))

  }

  test("Can convert seconds to Radians") {
    assertEquals(round(Radians.fromSeconds(Seconds(0))), round(Radians.zero))
    assertEquals(round(Radians.fromSeconds(Seconds(0.5))), round(Radians.PI))
    assertEquals(round(Radians.fromSeconds(Seconds(1))), round(Radians.zero))
    assertEquals(round(Radians.fromSeconds(Seconds(1.5))), round(Radians.PI))
  }

  def round(d: Radians): Double =
    Math.floor(d.value * 100d) / 100d

}
