package indigo.shared.animation

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.dice.Dice

final class AnimationKey(val value: String) extends AnyVal {
  override def toString(): String =
    s"AnimationKey($value)"
}

object AnimationKey {

  implicit val animationKeyEqualTo: EqualTo[AnimationKey] =
    EqualTo.create { (a, b) =>
      a.value === b.value
    }

  def apply(key: String): AnimationKey =
    new AnimationKey(key)

  def fromDice(dice: Dice): AnimationKey =
    AnimationKey(dice.rollAlphaNumeric)
}
