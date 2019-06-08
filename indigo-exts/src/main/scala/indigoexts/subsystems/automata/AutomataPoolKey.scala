package indigoexts.subsystems.automata

import indigo.shared.EqualTo
import indigo.shared.datatypes.BindingKey

final class AutomataPoolKey(val key: String) extends AnyVal {
  override def toString: String =
    s"AutomataPoolKey(key = $key)"
}
object AutomataPoolKey {

  implicit val eq: EqualTo[AutomataPoolKey] =
    EqualTo.create { (a, b) =>
      implicitly[EqualTo[String]].equal(a.key, b.key)
    }

  def apply(key: String): AutomataPoolKey =
    new AutomataPoolKey(key)

  def generate: AutomataPoolKey =
    AutomataPoolKey(BindingKey.generate.value)

}
