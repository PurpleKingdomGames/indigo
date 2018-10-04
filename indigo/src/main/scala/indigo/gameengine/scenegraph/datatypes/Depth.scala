package indigo.gameengine.scenegraph.datatypes

case class Depth(zIndex: Int) extends AnyVal {
  def +(other: Depth): Depth =
    Depth.append(this, other)
}
object Depth {
  val Base: Depth = Depth(1)

  implicit def intToDepth(i: Int): Depth = Depth(i)

  def append(a: Depth, b: Depth): Depth =
    Depth(a.zIndex + b.zIndex)
}
