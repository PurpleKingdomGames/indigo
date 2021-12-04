package indigo.shared.datatypes

import annotation.targetName

opaque type Depth = Int
object Depth:
  inline def apply(depth: Int): Depth = depth

  val zero: Depth = Depth(0)
  val near: Depth = zero
  val far: Depth = Depth(Int.MaxValue)

  extension (d: Depth)
    def +(other: Depth): Depth = Depth(d + other)
    @targetName("+_Int")
    def +(other: Int): Depth = Depth(d + other)

    def -(other: Depth): Depth = Depth(d - other)
    @targetName("-_Int")
    def -(other: Int): Depth = Depth(d - other)

    def toDouble: Double = d
    def toInt: Int       = d.toInt
