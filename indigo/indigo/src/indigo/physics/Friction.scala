package indigo.physics

import indigo.Vector2

import scala.annotation.targetName

opaque type Friction = Double

object Friction:
  inline def apply(Friction: Double): Friction = Friction
  val one: Friction                            = 1.0d
  val zero: Friction                           = 0.0d

  extension (m: Friction)
    inline def toDouble: Double  = m
    def *(vec: Vector2): Vector2 = vec * m.toDouble

    def +(other: Friction): Friction = other.toDouble + m.toDouble
    def -(other: Friction): Friction = other.toDouble - m.toDouble
    def *(other: Friction): Friction = other.toDouble * m.toDouble
    def /(other: Friction): Friction = other.toDouble / m.toDouble

    @targetName("Friction_add_double")
    def +(d: Double): Friction = m.toDouble + d
    @targetName("Friction_sub_double")
    def -(d: Double): Friction = m.toDouble - d
    @targetName("Friction_mul_double")
    def *(d: Double): Friction = m.toDouble * d
    @targetName("Friction_div_double")
    def /(d: Double): Friction = m.toDouble / d

    def ~==(other: Friction): Boolean =
      Math.abs(m.toDouble - other.toDouble) < 0.0001
