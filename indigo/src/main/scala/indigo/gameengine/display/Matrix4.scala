package indigo.gameengine.display

import indigo.shared.AsString
import indigo.shared.EqualTo

final case class Matrix4(mat: List[Double]) {

  def row1: List[Double] = List(mat(0), mat(1), mat(2), mat(3))
  def row2: List[Double] = List(mat(4), mat(5), mat(6), mat(7))
  def row3: List[Double] = List(mat(8), mat(9), mat(10), mat(11))
  def row4: List[Double] = List(mat(12), mat(13), mat(14), mat(15))

  def identity: Matrix4 = Matrix4.identity

  def translate(tx: Double, ty: Double, tz: Double): Matrix4 =
    Matrix4.multiply(this, Matrix4.translation(tx, ty, tz))

  def xRotate(angleInRadians: Double): Matrix4 = this * Matrix4.xRotation(angleInRadians)

  def yRotate(angleInRadians: Double): Matrix4 = this * Matrix4.yRotation(angleInRadians)

  def zRotate(angleInRadians: Double): Matrix4 = this * Matrix4.zRotation(angleInRadians)

  def scale(sx: Double, sy: Double, sz: Double): Matrix4 = this * Matrix4.scale(sx, sy, sz)

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

}

object Matrix4 {

  implicit val show: AsString[Matrix4] = {
    val ev = implicitly[AsString[List[Double]]]

    AsString.create { v =>
      s"Matrix4(${ev.show(v.row1)}, ${ev.show(v.row2)}, ${ev.show(v.row3)}, ${ev.show(v.row4)})"
    }
  }

  implicit val eq: EqualTo[Matrix4] = {
    val ev = implicitly[EqualTo[List[Double]]]

    EqualTo.create { (a, b) =>
      ev.equal(a.mat, b.mat)
    }
  }

  def identity: Matrix4 = Matrix4(
    mat = List(
      1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1
    )
  )

  def one: Matrix4 = Matrix4(
    mat = List(
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    )
  )

  /**
    * Usage Matrix.projection(2 * aspectRatio, 2, 2) (assuming width > height) because the screen by default is 2 x 2 units: -1 to 1
    */
  def projection(width: Double, height: Double, depth: Double): Matrix4 =
    Matrix4(
      List(
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
    )

  def orthographic(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double): Matrix4 =
    Matrix4(
      List(
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
    )

  def orthographic(width: Double, height: Double): Matrix4 =
    orthographic(0, width, height, 0, -10000, 10000)

  def translation(tx: Double, ty: Double, tz: Double): Matrix4 =
    Matrix4(
      List(
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
    )

  def xRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
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
    )
  }

  def yRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
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
    )
  }

  def zRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
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
    )
  }

  def scale(sx: Double, sy: Double, sz: Double): Matrix4 =
    Matrix4(
      List(
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
    )

  def translateAndScale(tx: Double, ty: Double, tz: Double, sx: Double, sy: Double, sz: Double): Matrix4 =
    Matrix4(
      List(
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
    )

  def multiply(a: Matrix4, b: Matrix4): Matrix4 = {
    val listA = a.mat
    val ListB = b.mat

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

    val b00 = ListB(0 * 4 + 0)
    val b01 = ListB(0 * 4 + 1)
    val b02 = ListB(0 * 4 + 2)
    val b03 = ListB(0 * 4 + 3)
    val b10 = ListB(1 * 4 + 0)
    val b11 = ListB(1 * 4 + 1)
    val b12 = ListB(1 * 4 + 2)
    val b13 = ListB(1 * 4 + 3)
    val b20 = ListB(2 * 4 + 0)
    val b21 = ListB(2 * 4 + 1)
    val b22 = ListB(2 * 4 + 2)
    val b23 = ListB(2 * 4 + 3)
    val b30 = ListB(3 * 4 + 0)
    val b31 = ListB(3 * 4 + 1)
    val b32 = ListB(3 * 4 + 2)
    val b33 = ListB(3 * 4 + 3)

    Matrix4(
      List(
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
    )
  }

  def transpose(matrix4: Matrix4): Matrix4 = {
    val m = matrix4.mat
    Matrix4(
      mat = List(
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

}
