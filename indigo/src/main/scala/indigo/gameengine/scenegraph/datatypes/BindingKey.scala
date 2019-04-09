package indigo.gameengine.scenegraph.datatypes

import scala.util.Random

import indigo.shared.EqualTo._
import indigo.shared.AsString

final class BindingKey(val value: String) extends AnyVal {

  def ===(other: BindingKey): Boolean =
    BindingKey.equality(this, other)

  def =!=(other: BindingKey): Boolean =
    !BindingKey.equality(this, other)

}

object BindingKey {
  
  def apply(value: String): BindingKey =
    new BindingKey(value)

  def generate: BindingKey =
    BindingKey(Random.alphanumeric.take(16).mkString)

  def equality(a: BindingKey, b: BindingKey): Boolean =
    a.value === b.value

    implicit val bindingKeyAsString: AsString[BindingKey] =
      AsString.create { k =>
        s"""BindingKey(${k.value})"""
      }

}
