package indigo.shared.datatypes

import indigo.shared.EqualTo
import indigo.shared.dice.Dice

final case class BindingKey(value: String) extends AnyVal {

  @SuppressWarnings(Array("org.wartremover.warts.Equals"))
  def ===(other: BindingKey): Boolean =
    implicitly[EqualTo[BindingKey]].equal(this, other)
    
}

object BindingKey {

  def fromDice(dice: Dice): BindingKey =
    BindingKey(dice.rollAlphaNumeric)

  implicit val eq: EqualTo[BindingKey] = {
    val eqS = implicitly[EqualTo[String]]
    EqualTo.create { (a, b) =>
      eqS.equal(a.value, b.value)
    }
  }

}
