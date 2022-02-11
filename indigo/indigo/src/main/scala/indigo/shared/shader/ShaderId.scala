package indigo.shared.shader

import indigo.shared.dice.Dice

/** Represents the id of a shader, and used to bind an entity/material to that particular shader. Shader ID's must be
  * unique or collisions will occur.
  */
opaque type ShaderId = String

object ShaderId:
  inline def apply(value: String): ShaderId =
    value
  inline def fromDice(dice: Dice): ShaderId =
    dice.rollAlphaNumeric
  inline def generate(dice: Dice): ShaderId =
    dice.rollAlphaNumeric
  extension (sid: ShaderId) inline def toString: String = sid

  given CanEqual[ShaderId, ShaderId]                 = CanEqual.derived
  given CanEqual[Option[ShaderId], Option[ShaderId]] = CanEqual.derived
