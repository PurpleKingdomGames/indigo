package indigo.gameengine.display

final case class Vector4(x: Double, y: Double, z: Double, w: Double) {

  def translate(vec: Vector4): Vector4 =
    Vector4.add(this, vec)

  def scale(vec: Vector4): Vector4 =
    Vector4.multiply(this, vec)

  def round: Vector4 = Vector4(Math.round(x).toDouble, Math.round(y).toDouble, Math.round(z).toDouble, Math.round(w).toDouble)

  def toList: List[Double] = List(x, y, z, w)

  def +(other: Vector4): Vector4 = Vector4.add(this, other)
  def -(other: Vector4): Vector4 = Vector4.subtract(this, other)
  def *(other: Vector4): Vector4 = Vector4.multiply(this, other)
  def /(other: Vector4): Vector4 = Vector4.divide(this, other)

  def applyMatrix4(matrix4: Matrix4): Vector4 = Vector4.applyMatrix4(this, matrix4)
}

object Vector4 {

  val zero: Vector4 = Vector4(0d, 0d, 0d, 0d)
  val one: Vector4  = Vector4(1d, 1d, 1d, 1d)

  def add(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x + vec2.x, vec1.y + vec2.y, vec1.z + vec2.z, vec1.w + vec2.w)

  def subtract(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z, vec1.w - vec2.w)

  def multiply(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x * vec2.x, vec1.y * vec2.y, vec1.z * vec2.z, vec1.w * vec2.w)

  def divide(vec1: Vector4, vec2: Vector4): Vector4 =
    Vector4(vec1.x / vec2.x, vec1.y / vec2.y, vec1.z / vec2.z, vec1.w / vec2.w)

  def position(x: Double, y: Double, z: Double): Vector4 = Vector4(x, y, z, 1)

  def direction(x: Double, y: Double, z: Double): Vector4 = Vector4(x, y, z, 0)

  def applyMatrix4(vector4: Vector4, matrix4: Matrix4): Vector4 = {
    val m  = matrix4.transpose
    val vl = vector4.toList

    Vector4(
      x = m.row1.zip(vl).map(p => p._1 * p._2).sum,
      y = m.row2.zip(vl).map(p => p._1 * p._2).sum,
      z = m.row3.zip(vl).map(p => p._1 * p._2).sum,
      w = m.row4.zip(vl).map(p => p._1 * p._2).sum
    )
  }

}
