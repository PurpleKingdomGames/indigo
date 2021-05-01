package indigo.shared.animation

import indigo.shared.dice.Dice

opaque type AnimationKey = String

object AnimationKey:
  def apply(key: String): AnimationKey = key
  def fromDice(dice: Dice): AnimationKey =
    AnimationKey(dice.rollAlphaNumeric)
