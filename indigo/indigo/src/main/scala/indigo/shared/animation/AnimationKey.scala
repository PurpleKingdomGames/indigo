package indigo.shared.animation

import indigo.shared.dice.Dice

opaque type AnimationKey = String

object AnimationKey:
  inline def apply(key: String): AnimationKey = key
  inline def fromDice(dice: Dice): AnimationKey =
    dice.rollAlphaNumeric

  extension (a: AnimationKey) inline def toString: String = a
