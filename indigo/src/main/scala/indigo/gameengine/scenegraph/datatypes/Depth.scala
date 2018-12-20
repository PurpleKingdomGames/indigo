package indigo.gameengine.scenegraph.datatypes

final case class Depth(zIndex: Int) extends AnyVal {
  def +(other: Depth): Depth =
    Depth.append(this, other)
}
object Depth {
  val Base: Depth = Depth(1)

  def append(a: Depth, b: Depth): Depth =
    Depth(a.zIndex + b.zIndex)
}
