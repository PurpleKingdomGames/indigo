package indigo.shared.datatypes

import annotation.targetName

opaque type Depth = Int
object Depth:
  inline def apply(depth: Int): Depth = depth

  val Zero: Depth = Depth(0)
  val Base: Depth = Depth(1)
  val one: Depth  = Base

  extension (d: Depth)
    def +(other: Depth): Depth = Depth(d + other)
    @targetName("+_Int")
    def +(other: Int): Depth = Depth(d + other)

    def -(other: Depth): Depth = Depth(d - other)
    @targetName("-_Int")
    def -(other: Int): Depth = Depth(d - other)

    inline def toDouble: Double = d
    inline def toInt: Int       = d.toInt
