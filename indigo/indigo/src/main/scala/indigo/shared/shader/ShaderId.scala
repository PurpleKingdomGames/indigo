package indigo.shared.shader

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
