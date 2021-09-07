package indigo.shared.shader

import indigo.shared.dice.Dice

/** Represents the id of a shader, and used to bind an entity/material to that particular shader. Shader ID's must be
  * unique or collisions will occur.
  */
opaque type ShaderId = String

object ShaderId:
  def apply(value: String): ShaderId =
    value
  def fromDice(dice: Dice): ShaderId =
    ShaderId(dice.rollAlphaNumeric)
  def generate(dice: Dice): ShaderId =
    fromDice(dice)

  given CanEqual[ShaderId, ShaderId]                 = CanEqual.derived
  given CanEqual[Option[ShaderId], Option[ShaderId]] = CanEqual.derived
