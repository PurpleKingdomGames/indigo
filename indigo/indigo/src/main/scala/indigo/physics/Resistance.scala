package indigo.physics

import indigo.Vector2

import scala.annotation.targetName

/** Resistance is the general resistance to movement in the atmosphere, be that wind resistance or water resistance.
  */
opaque type Resistance = Double

object Resistance:
  inline def apply(Resistance: Double): Resistance = Resistance
  val one: Resistance                              = 1.0d
  val zero: Resistance                             = 0.0d

  extension (m: Resistance)
    inline def toDouble: Double  = m
    def *(vec: Vector2): Vector2 = vec * m.toDouble

    def +(other: Resistance): Resistance = other.toDouble + m.toDouble
    def -(other: Resistance): Resistance = other.toDouble - m.toDouble
    def *(other: Resistance): Resistance = other.toDouble * m.toDouble
    def /(other: Resistance): Resistance = other.toDouble / m.toDouble

    @targetName("Friction_add_double")
    def +(d: Double): Resistance = m.toDouble + d
    @targetName("Friction_sub_double")
    def -(d: Double): Resistance = m.toDouble - d
    @targetName("Friction_mul_double")
    def *(d: Double): Resistance = m.toDouble * d
    @targetName("Friction_div_double")
    def /(d: Double): Resistance = m.toDouble / d

    def ~==(other: Resistance): Boolean =
      Math.abs(m.toDouble - other.toDouble) < 0.0001
