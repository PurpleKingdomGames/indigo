package indigo.shared.subsystems

import indigo.shared.dice.Dice

opaque type SubSystemId = String

object SubSystemId:
  inline def apply(value: String): SubSystemId =
    value
  inline def fromDice(dice: Dice): SubSystemId =
    dice.rollAlphaNumeric
  inline def generate(dice: Dice): SubSystemId =
    dice.rollAlphaNumeric

  extension (b: SubSystemId) inline def toString: String = b

  given CanEqual[SubSystemId, SubSystemId]                 = CanEqual.derived
  given CanEqual[Option[SubSystemId], Option[SubSystemId]] = CanEqual.derived
