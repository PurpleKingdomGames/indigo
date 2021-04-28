package indigo.shared.datatypes

import indigo.shared.dice.Dice

opaque type BindingKey = String

object BindingKey:
  def apply(value: String): BindingKey = value
  def fromDice(dice: Dice): BindingKey =
    BindingKey(dice.rollAlphaNumeric)
