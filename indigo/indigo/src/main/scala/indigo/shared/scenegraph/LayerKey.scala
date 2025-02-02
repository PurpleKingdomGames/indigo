package indigo.shared.scenegraph

import indigo.shared.dice.Dice

opaque type LayerKey = String

object LayerKey:
  inline def apply(value: String): LayerKey =
    value
  inline def fromDice(dice: Dice): LayerKey =
    dice.rollAlphaNumeric
  inline def generate(dice: Dice): LayerKey =
    dice.rollAlphaNumeric

  extension (b: LayerKey) inline def toString: String = b

  given CanEqual[LayerKey, LayerKey]                 = CanEqual.derived
  given CanEqual[Option[LayerKey], Option[LayerKey]] = CanEqual.derived
