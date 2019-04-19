package indigo.gameengine.display

import indigo.shared.AsString
import indigo.shared.EqualTo

final class Vector2(val x: Double, val y: Double) {

  def translate(vec: Vector2): Vector2 =
    Vector2.add(this, vec)

  def scale(vec: Vector2): Vector2 =
    Vector2.multiply(this, vec)

  def round: Vector2 = Vector2(Math.round(x).toDouble, Math.round(y).toDouble)

  def +(other: Vector2): Vector2 = Vector2.add(this, other)
  def -(other: Vector2): Vector2 = Vector2.subtract(this, other)
  def *(other: Vector2): Vector2 = Vector2.multiply(this, other)
  def /(other: Vector2): Vector2 = Vector2.divide(this, other)
}

object Vector2 {

  implicit val show: AsString[Vector2] = {
    val sD = implicitly[AsString[Double]]

    AsString.create { v =>
      s"Vector2(x = ${sD.show(v.x)}, y = ${sD.show(v.y)})"
    }
  }

  implicit val eq: EqualTo[Vector2] = {
    val ev = implicitly[EqualTo[Double]]

    EqualTo.create { (a, b) =>
      ev.equal(a.x, b.x) && ev.equal(a.y, b.y)
    }
  }

  def apply(x: Double, y: Double): Vector2 =
    new Vector2(x, y)

  val zero: Vector2 = Vector2(0d, 0d)
  val one: Vector2  = Vector2(1d, 1d)

  def add(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x + vec2.x, vec1.y + vec2.y)

  def subtract(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x - vec2.x, vec1.y - vec2.y)

  def multiply(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x * vec2.x, vec1.y * vec2.y)

  def divide(vec1: Vector2, vec2: Vector2): Vector2 =
    Vector2(vec1.x / vec2.x, vec1.y / vec2.y)

  def apply(i: Int): Vector2 = Vector2(i.toDouble, i.toDouble)

}
