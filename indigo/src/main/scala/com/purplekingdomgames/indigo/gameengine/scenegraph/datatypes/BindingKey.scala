package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import scala.util.Random

case class BindingKey(value: String) {

  def ===(other: BindingKey): Boolean =
    BindingKey.equality(this, other)

}

object BindingKey {
  private val random: Random = new Random

  def generate: BindingKey = BindingKey(random.alphanumeric.take(16).mkString)

  def equality(a: BindingKey, b: BindingKey): Boolean =
    a.value == b.value
}
