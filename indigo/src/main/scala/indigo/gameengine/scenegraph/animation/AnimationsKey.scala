package indigo.gameengine.scenegraph.animation

import indigo.EqualTo._

final case class AnimationsKey(key: String) extends AnyVal {
  def ===(other: AnimationsKey): Boolean =
    AnimationsKey.equality(this, other)
}
object AnimationsKey {
  def equality(a: AnimationsKey, b: AnimationsKey): Boolean =
    a.key === b.key
}