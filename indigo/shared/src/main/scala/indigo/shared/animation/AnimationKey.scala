package indigo.shared.animation

import indigo.shared.EqualTo
import indigo.shared.EqualTo._
import indigo.shared.dice.Dice

final case class AnimationKey(value: String) extends AnyVal

object AnimationKey {

  implicit val animationKeyEqualTo: EqualTo[AnimationKey] =
    EqualTo.create { (a, b) =>
      a.value === b.value
    }

  def fromDice(dice: Dice): AnimationKey =
    AnimationKey(dice.rollAlphaNumeric)
}
