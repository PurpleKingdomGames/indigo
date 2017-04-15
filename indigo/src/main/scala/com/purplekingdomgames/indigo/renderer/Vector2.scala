package com.purplekingdomgames.indigo.renderer

case class Vector2(x: Double, y: Double) {

  def translate(vec: Vector2): Vector2 = {
    Vector2.add(this, vec)
  }

  def scale(vec: Vector2): Vector2 = {
    Vector2.multiply(this, vec)
  }

  def round: Vector2 = Vector2(Math.round(x), Math.round(y))

  def toScalaJSArrayDouble: scalajs.js.Array[Double] = scalajs.js.Array[Double](x, y)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)
}

object Vector2 {

  val zero: Vector2 = Vector2(0, 0)
  val one: Vector2 = Vector2(1, 1)

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

}
