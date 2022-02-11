package indigo.shared.datatypes

import indigo.shared.dice.Dice

opaque type BindingKey = String

object BindingKey:
  inline def apply(value: String): BindingKey =
    value
  inline def fromDice(dice: Dice): BindingKey =
    dice.rollAlphaNumeric
  inline def generate(dice: Dice): BindingKey =
    dice.rollAlphaNumeric

  extension (b: BindingKey) inline def toString: String = b

  given CanEqual[BindingKey, BindingKey]                 = CanEqual.derived
  given CanEqual[Option[BindingKey], Option[BindingKey]] = CanEqual.derived
