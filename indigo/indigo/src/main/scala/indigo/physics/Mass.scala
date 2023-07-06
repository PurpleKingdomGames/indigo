package indigo.physics

import indigo.Vector2

import scala.annotation.targetName

opaque type Mass = Double

object Mass:
  inline def apply(mass: Double): Mass = mass
  val one: Mass                        = 1.0d
  val default: Mass                    = one

  extension (m: Mass)
    inline def toDouble: Double  = m
    def *(vec: Vector2): Vector2 = vec * m.toDouble

    def +(other: Mass): Mass = m.toDouble + other.toDouble
    def -(other: Mass): Mass = m.toDouble - other.toDouble
    def *(other: Mass): Mass = m.toDouble * other.toDouble
    def /(other: Mass): Mass = m.toDouble / other.toDouble

    @targetName("mass_add_double")
    def +(d: Double): Mass = m.toDouble + d
    @targetName("mass_sub_double")
    def -(d: Double): Mass = m.toDouble - d
    @targetName("mass_mul_double")
    def *(d: Double): Mass = m.toDouble * d
    @targetName("mass_div_double")
    def /(d: Double): Mass = m.toDouble / d

    def ~==(other: Mass): Boolean =
      Math.abs(m.toDouble - other.toDouble) < 0.0001
