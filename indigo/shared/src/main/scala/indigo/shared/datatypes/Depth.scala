package indigo.shared.datatypes

final class Depth(val zIndex: Int) extends AnyVal {
  def +(other: Depth): Depth =
    Depth.append(this, other)
}
object Depth {
  val Base: Depth = Depth(1)
  val one: Depth = Base

  def apply(zIndex: Int): Depth =
    new Depth(zIndex)

  def append(a: Depth, b: Depth): Depth =
    Depth(a.zIndex + b.zIndex)
}
