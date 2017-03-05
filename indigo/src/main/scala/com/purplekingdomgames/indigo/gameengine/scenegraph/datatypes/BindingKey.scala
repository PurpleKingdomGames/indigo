package com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes

import scala.util.Random

case class BindingKey(value: String)
object BindingKey {
  private val random: Random = new Random

  def generate: BindingKey = BindingKey(random.alphanumeric.take(16).mkString)
}
