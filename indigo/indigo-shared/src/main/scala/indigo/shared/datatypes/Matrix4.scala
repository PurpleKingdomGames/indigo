package indigo.shared.datatypes

import util.control.Breaks._

final case class Matrix4(private val mat: List[Double]) {

  lazy val row1: List[Double] = List(mat(0), mat(1), mat(2), mat(3))
  lazy val row2: List[Double] = List(mat(4), mat(5), mat(6), mat(7))
  lazy val row3: List[Double] = List(mat(8), mat(9), mat(10), mat(11))
  lazy val row4: List[Double] = List(mat(12), mat(13), mat(14), mat(15))

  lazy val col1: List[Double] = List(mat(0), mat(4), mat(8), mat(12))
  lazy val col2: List[Double] = List(mat(1), mat(5), mat(9), mat(13))
  lazy val col3: List[Double] = List(mat(2), mat(6), mat(10), mat(14))
  lazy val col4: List[Double] = List(mat(3), mat(7), mat(11), mat(15))

  lazy val x: Double = mat(12)
  lazy val y: Double = mat(13)
  lazy val z: Double = mat(14)

  lazy val data: (List[Double], List[Double]) =
    (List(mat(0), mat(1),mat(4), mat(5)), List(mat(12), mat(13),mat(14)))

  def identity: Matrix4 = Matrix4.identity

  def translate(by: Vector3): Matrix4 =
    this * Matrix4.translation(by.x, by.y, by.z)

  def rotate(angle: Radians): Matrix4 =
    this * Matrix4.zRotation(angle.value)

  def scale(by: Vector3): Matrix4 = 
  this * Matrix4.scale(by.x, by.y, by.z)

  def transpose: Matrix4 = Matrix4.transpose(this)

  def *(other: Matrix4): Matrix4 = Matrix4.multiply(this, other)

  def projection(width: Double, height: Double, depth: Double): Matrix4 =
    this * Matrix4.projection(width, height, depth)

  def orthographic(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double): Matrix4 =
    this * Matrix4.orthographic(left, right, bottom, top, near, far)

  def withOrthographic(orthographic: Matrix4): Matrix4 =
    this * orthographic

  def flip(horizontal: Boolean, vertical: Boolean): Matrix4 =
    this * Matrix4.flip(horizontal, vertical)

  def toList: List[Double] =
    mat

  def transform(vector: Vector3): Vector3 =
    Vector3(
      x = col1(0) * vector.x + col1(1) * vector.y + col1(2) * vector.z + col1(3),
      y = col2(0) * vector.x + col2(1) * vector.y + col2(2) * vector.z + col2(3),
      z = col3(0) * vector.x + col3(1) * vector.y + col3(2) * vector.z + col3(3)
    )

  def prettyPrint: String =
    row1.mkString("(", ",\t", ")") + "\n" +
      row2.mkString("(", ",\t", ")") + "\n" +
      row3.mkString("(", ",\t", ")") + "\n" +
      row4.mkString("(", ",\t", ")")

  def ~==(other: Matrix4): Boolean = {
    if(mat.length == other.mat.length)
      var count = mat.length - 1
      var same = true
      while(count > 0) {
        breakable {
        if(Math.abs(mat(count) - other.mat(count)) > 0.001)
          same = false
          break
        }
        count = count - 1
      }
      same
    else
      false
  }
}

object Matrix4 {

  val identity: Matrix4 =
    Matrix4(
      (1, 0, 0, 0),
      (0, 1, 0, 0),
      (0, 0, 1, 0),
      (0, 0, 0, 1)
    )

  val one: Matrix4 =
    Matrix4(
      (1, 1, 1, 1),
      (1, 1, 1, 1),
      (1, 1, 1, 1),
      (1, 1, 1, 1)
    )

  /**
    * Usage Matrix.projection(2 * aspectRatio, 2, 2) (assuming width > height) because the screen by default is 2 x 2 units: -1 to 1
    */
  def projection(width: Double, height: Double, depth: Double): Matrix4 =
    Matrix4(
      2 / width,
      0,
      0,
      0,
      0,
      2 / height,
      0,
      0,
      0,
      0,
      2 / depth,
      0,
      0,
      0,
      0,
      1
    )

  def orthographic(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double): Matrix4 =
    Matrix4(
      2 / (right - left),
      0,
      0,
      0,
      0,
      2 / (top - bottom),
      0,
      0,
      0,
      0,
      2 / (near - far),
      0,
      (left + right) / (left - right),
      (bottom + top) / (bottom - top),
      (near + far) / (near - far),
      1
    )

  def orthographic(width: Double, height: Double): Matrix4 =
    orthographic(0, width, height, 0, -10000, 10000)

  def transform2d(tx: Double, ty: Double, sx: Double, sy: Double, r: Double): Matrix4 = {
    val c = Math.cos(r)
    val s = Math.sin(r)

    Matrix4(
      sx * c,
      s,
      0,
      0,
      -s,
      sy * c,
      0,
      0,
      0,
      0,
      1,
      0,
      tx,
      ty,
      0,
      1
    )
  }

  def translation(amount: Vector3): Matrix4 =
    translation(amount.x, amount.y, amount.z)
  def translation(tx: Double, ty: Double, tz: Double): Matrix4 =
    Matrix4(
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1,
      0,
      tx,
      ty,
      tz,
      1
    )

  def xRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      1,
      0,
      0,
      0,
      0,
      c,
      s,
      0,
      0,
      -s,
      c,
      0,
      0,
      0,
      0,
      1
    )
  }

  def yRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      c,
      0,
      -s,
      0,
      0,
      1,
      0,
      0,
      s,
      0,
      c,
      0,
      0,
      0,
      0,
      1
    )
  }

  def zRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      c,
      s,
      0,
      0,
      -s,
      c,
      0,
      0,
      0,
      0,
      1,
      0,
      0,
      0,
      0,
      1
    )
  }

  def scale(amount: Vector3): Matrix4 =
    scale(amount.x, amount.y, amount.z)
  def scale(sx: Double, sy: Double, sz: Double): Matrix4 =
    Matrix4(
      sx,
      0,
      0,
      0,
      0,
      sy,
      0,
      0,
      0,
      0,
      sz,
      0,
      0,
      0,
      0,
      1
    )

  def translateAndScale(tx: Double, ty: Double, tz: Double, sx: Double, sy: Double, sz: Double): Matrix4 =
    Matrix4(
      sx,
      0,
      0,
      0,
      0,
      sy,
      0,
      0,
      0,
      0,
      sz,
      0,
      tx,
      ty,
      tz,
      1
    )

  def multiply(a: Matrix4, b: Matrix4): Matrix4 = {
    val listA = a.mat
    val listB = b.mat

    val a00 = listA(0 * 4 + 0)
    val a01 = listA(0 * 4 + 1)
    val a02 = listA(0 * 4 + 2)
    val a03 = listA(0 * 4 + 3)
    val a10 = listA(1 * 4 + 0)
    val a11 = listA(1 * 4 + 1)
    val a12 = listA(1 * 4 + 2)
    val a13 = listA(1 * 4 + 3)
    val a20 = listA(2 * 4 + 0)
    val a21 = listA(2 * 4 + 1)
    val a22 = listA(2 * 4 + 2)
    val a23 = listA(2 * 4 + 3)
    val a30 = listA(3 * 4 + 0)
    val a31 = listA(3 * 4 + 1)
    val a32 = listA(3 * 4 + 2)
    val a33 = listA(3 * 4 + 3)

    val b00 = listB(0 * 4 + 0)
    val b01 = listB(0 * 4 + 1)
    val b02 = listB(0 * 4 + 2)
    val b03 = listB(0 * 4 + 3)
    val b10 = listB(1 * 4 + 0)
    val b11 = listB(1 * 4 + 1)
    val b12 = listB(1 * 4 + 2)
    val b13 = listB(1 * 4 + 3)
    val b20 = listB(2 * 4 + 0)
    val b21 = listB(2 * 4 + 1)
    val b22 = listB(2 * 4 + 2)
    val b23 = listB(2 * 4 + 3)
    val b30 = listB(3 * 4 + 0)
    val b31 = listB(3 * 4 + 1)
    val b32 = listB(3 * 4 + 2)
    val b33 = listB(3 * 4 + 3)

    Matrix4(
      a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30,
      a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31,
      a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32,
      a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33,
      a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30,
      a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31,
      a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32,
      a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33,
      a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30,
      a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31,
      a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32,
      a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33,
      a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30,
      a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31,
      a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32,
      a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33
    )
  }

  def transpose(matrix4: Matrix4): Matrix4 = {
    val m = matrix4.mat
    Matrix4(
      m(0),
      m(4),
      m(8),
      m(12),
      m(1),
      m(5),
      m(9),
      m(13),
      m(2),
      m(6),
      m(10),
      m(14),
      m(3),
      m(7),
      m(11),
      m(15)
    )
  }

  def flip(horizontal: Boolean, vertical: Boolean): Matrix4 =
    (horizontal, vertical) match {
      case (true, true)   => Matrix4.scale(-1, -1, -1)
      case (true, false)  => Matrix4.scale(-1, 1, -1)
      case (false, true)  => Matrix4.scale(1, -1, -1)
      case (false, false) => Matrix4.identity
    }

  def apply(): Matrix4 = identity

  def apply(
      row0: (Double, Double, Double, Double),
      row1: (Double, Double, Double, Double),
      row2: (Double, Double, Double, Double),
      row3: (Double, Double, Double, Double)
  ): Matrix4 =
    Matrix4(
      List(row0._1, row0._2, row0._3, row0._4) ++
        List(row1._1, row1._2, row1._3, row1._4) ++
        List(row2._1, row2._2, row2._3, row2._4) ++
        List(row3._1, row3._2, row3._3, row3._4)
    )

  def apply(
      a1: Double,
      a2: Double,
      a3: Double,
      a4: Double,
      b1: Double,
      b2: Double,
      b3: Double,
      b4: Double,
      c1: Double,
      c2: Double,
      c3: Double,
      c4: Double,
      d1: Double,
      d2: Double,
      d3: Double,
      d4: Double
  ): Matrix4 =
    Matrix4(
      List(
        a1,
        a2,
        a3,
        a4,
        b1,
        b2,
        b3,
        b4,
        c1,
        c2,
        c3,
        c4,
        d1,
        d2,
        d3,
        d4
      )
    )
}
