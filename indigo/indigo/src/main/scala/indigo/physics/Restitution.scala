package indigo.physics

import indigo.Vector2

import scala.annotation.targetName

opaque type Restitution = Double

object Restitution:
  inline def apply(Restitution: Double): Restitution = Restitution
  val one: Restitution                               = 1.0d
  val zero: Restitution                              = 0.0d
  val default: Restitution                           = one

  extension (m: Restitution)
    inline def toDouble: Double  = m
    def *(vec: Vector2): Vector2 = vec * m.toDouble

    def +(other: Restitution): Restitution = other.toDouble + m.toDouble
    def -(other: Restitution): Restitution = other.toDouble - m.toDouble
    def *(other: Restitution): Restitution = other.toDouble * m.toDouble
    def /(other: Restitution): Restitution = other.toDouble / m.toDouble

    @targetName("Restitution_add_double")
    def +(d: Double): Restitution = m.toDouble + d
    @targetName("Restitution_sub_double")
    def -(d: Double): Restitution = m.toDouble - d
    @targetName("Restitution_mul_double")
    def *(d: Double): Restitution = m.toDouble * d
    @targetName("Restitution_div_double")
    def /(d: Double): Restitution = m.toDouble / d

    def ~==(other: Restitution): Boolean =
      Math.abs(m.toDouble - other.toDouble) < 0.0001
