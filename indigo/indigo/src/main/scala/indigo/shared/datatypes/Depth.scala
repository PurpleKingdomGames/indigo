package indigo.shared.datatypes

opaque type Depth = Int
object Depth:
  def apply(depth: Int): Depth = depth

  val Zero: Depth = Depth(0)
  val Base: Depth = Depth(1)
  val one: Depth  = Base

  extension (d: Depth)
    def +(other: Depth): Depth = Depth(d + other)
    def toDouble: Double       = d
    def toInt: Int             = d.toInt
