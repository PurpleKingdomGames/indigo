package indigo.shared.datatypes

final case class Depth(zIndex: Int) extends AnyVal {
  def +(other: Depth): Depth =
    Depth(this.zIndex + other.zIndex)
}
object Depth {
  val Zero: Depth = Depth(0)
  val Base: Depth = Depth(1)
  val one: Depth = Base
}
