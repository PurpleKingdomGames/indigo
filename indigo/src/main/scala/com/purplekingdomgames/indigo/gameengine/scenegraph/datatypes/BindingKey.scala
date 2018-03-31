package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import scala.util.Random

case class BindingKey(value: String) extends AnyVal {

  def ===(other: BindingKey): Boolean =
    BindingKey.equality(this, other)

  def =!=(other: BindingKey): Boolean =
    !BindingKey.equality(this, other)

}

object BindingKey {
  def generate: BindingKey =
    BindingKey(Random.alphanumeric.take(16).mkString)

  def equality(a: BindingKey, b: BindingKey): Boolean =
    a.value == b.value
}
