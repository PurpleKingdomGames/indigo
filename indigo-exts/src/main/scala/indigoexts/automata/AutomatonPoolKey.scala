package indigoexts.automata

import indigo.EqualTo._
import indigo.gameengine.scenegraph.datatypes.BindingKey

final class AutomatonPoolKey(val key: String) extends AnyVal {
  def ===(other: AutomatonPoolKey): Boolean =
    AutomatonPoolKey.equality(this, other)
}
object AutomatonPoolKey {

  def apply(key: String): AutomatonPoolKey =
    new AutomatonPoolKey(key)

  def generate: AutomatonPoolKey =
    AutomatonPoolKey(BindingKey.generate.value)

  def equality(a: AutomatonPoolKey, b: AutomatonPoolKey): Boolean =
    a.key === b.key
}