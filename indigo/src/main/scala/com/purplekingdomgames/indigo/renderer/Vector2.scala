package com.purplekingdomgames.indigo.renderer

case class Vector2(x: Double, y: Double) {

  def translate(vec: Vector2): Vector2 = {
    Vector2.add(this, vec)
  }

  def scale(vec: Vector2): Vector2 = {
    Vector2.multiply(this, vec)
  }

  def round: Vector2 = Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def toScalaJSArrayDouble: scalajs.js.Array[Double] = scalajs.js.Array[Double](x, y)
  def toScalaJSArrayInt: scalajs.js.Array[Int] = scalajs.js.Array[Int](x.toInt, y.toInt)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)
}

object Vector2 {

  val zero: Vector2 = Vector2(0d, 0d)
  val one: Vector2 = Vector2(1d, 1d)

  def add(vec1: Vector2, vec2: Vector2): Vector2 = {
    Vector2(vec1.x + vec2.x, vec1.y + vec2.y)
  }

  def subtract(vec1: Vector2, vec2: Vector2): Vector2 = {
    Vector2(vec1.x - vec2.x, vec1.y - vec2.y)
  }

  def multiply(vec1: Vector2, vec2: Vector2): Vector2 = {
    Vector2(vec1.x * vec2.x, vec1.y * vec2.y)
  }

  def divide(vec1: Vector2, vec2: Vector2): Vector2 = {
    Vector2(vec1.x / vec2.x, vec1.y / vec2.y)
  }

  def apply(i: Int): Vector2 = Vector2(i.toDouble, i.toDouble)

}
