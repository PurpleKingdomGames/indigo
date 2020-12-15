package indigo.shared.datatypes

import indigo.shared.dice.Dice

final case class BindingKey(value: String) extends AnyVal

object BindingKey {

  def fromDice(dice: Dice): BindingKey =
    BindingKey(dice.rollAlphaNumeric)

}
