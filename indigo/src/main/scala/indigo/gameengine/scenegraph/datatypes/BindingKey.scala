package indigo.gameengine.scenegraph.datatypes

import scala.util.Random

import indigo.Eq._

final case class BindingKey(value: String) extends AnyVal {

  def ===(other: BindingKey): Boolean =
    BindingKey.equality(this, other)

  def =!=(other: BindingKey): Boolean =
    !BindingKey.equality(this, other)

}

object BindingKey {
  def generate: BindingKey =
    BindingKey(Random.alphanumeric.take(16).mkString)

  def equality(a: BindingKey, b: BindingKey): Boolean =
    a.value === b.value
}
