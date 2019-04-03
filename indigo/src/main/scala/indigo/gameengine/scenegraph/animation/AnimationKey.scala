package indigo.gameengine.scenegraph.animation

import indigo.shared.EqualTo._

final class AnimationKey(val key: String) extends AnyVal {
  def ===(other: AnimationKey): Boolean =
    AnimationKey.equality(this, other)
}
object AnimationKey {
  def equality(a: AnimationKey, b: AnimationKey): Boolean =
    a.key === b.key

  def apply(key: String): AnimationKey =
    new AnimationKey(key)
}
