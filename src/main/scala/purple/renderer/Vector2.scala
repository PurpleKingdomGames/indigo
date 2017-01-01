package purple.renderer

case class Vector2(x: Double, y: Double) {

  def translate(vec: Vector2): Vector2 = {
    Vector2.add(this, vec)
  }

  def scale(vec: Vector2): Vector2 = {
    Vector2.multiply(this, vec)
  }

  def round: Vector2 = Vector2(Math.round(x), Math.round(y))

  def toScalaJSArrayDouble: scalajs.js.Array[Double] = scalajs.js.Array[Double](x, y)
}

object Vector2 {

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
