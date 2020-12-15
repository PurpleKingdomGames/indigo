package indigo.shared.animation

import indigo.shared.dice.Dice

final case class AnimationKey(value: String) extends AnyVal

object AnimationKey {
  def fromDice(dice: Dice): AnimationKey =
    AnimationKey(dice.rollAlphaNumeric)
}
