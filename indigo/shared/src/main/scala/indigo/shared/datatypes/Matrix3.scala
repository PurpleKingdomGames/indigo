package indigo.shared.datatypes

import indigo.shared.EqualTo

final case class Matrix3(private val mat: List[Double]) {

  def row1: List[Double] = List(mat(0), mat(1), mat(2))
  def row2: List[Double] = List(mat(3), mat(4), mat(5))
  def row3: List[Double] = List(mat(6), mat(7), mat(8))

  def identity: Matrix3 =
    Matrix3.identity

  def translate(tx: Double, ty: Double): Matrix3 =
    this * Matrix3.translation(tx, ty)

  def rotate(angleInRadians: Double): Matrix3 =
    this * Matrix3.rotation(angleInRadians)

  def scale(sx: Double, sy: Double): Matrix3 =
    this * Matrix3.scale(sx, sy)

  def transpose: Matrix3 =
    Matrix3.transpose(this)

  def *(other: Matrix3): Matrix3 =
    Matrix3.multiply(this, other)

  def flip(horizontal: Boolean, vertical: Boolean): Matrix3 =
    this * Matrix3.flip(horizontal, vertical)

  def toList: List[Double] =
    mat
}

object Matrix3 {

  implicit val eq: EqualTo[Matrix3] = {
    val ev = implicitly[EqualTo[List[Double]]]

    EqualTo.create { (a, b) =>
      ev.equal(a.mat, b.mat)
    }
  }

  val identity: Matrix3 =
    Matrix3(
      (1, 0, 0),
      (0, 1, 0),
      (0, 0, 1)
    )

  val one: Matrix3 =
    Matrix3(
      (1, 1, 1),
      (1, 1, 1),
      (1, 1, 1)
    )

  def transform2d(tx: Double, ty: Double, sx: Double, sy: Double, r: Double): Matrix3 = {
    val c = Math.cos(r)
    val s = Math.sin(r)

    Matrix3(
      sx * c,
      s,
      0,
      -s,
      sy * c,
      0,
      tx,
      ty,
      1
    )
  }

  def translation(tx: Double, ty: Double): Matrix3 =
    Matrix3(
      1,
      0,
      0,
      0,
      1,
      0,
      tx,
      ty,
      1
    )

  def rotation(angleInRadians: Double): Matrix3 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix3(
      c,
      s,
      0,
      -s,
      c,
      0,
      0,
      0,
      1
    )
  }

  def scale(sx: Double, sy: Double): Matrix3 =
    Matrix3(
      sx,
      0,
      0,
      0,
      sy,
      0,
      0,
      0,
      1
    )

  def translateAndScale(tx: Double, ty: Double, sx: Double, sy: Double): Matrix3 =
    Matrix3(
      sx,
      0,
      0,
      0,
      sy,
      0,
      tx,
      ty,
      1
    )

  def multiply(a: Matrix3, b: Matrix3): Matrix3 = {
    val listA = a.mat
    val listB = b.mat

    val a00 = listA(0 * 3 + 0)
    val a01 = listA(0 * 3 + 1)
    val a02 = listA(0 * 3 + 2)
    val a10 = listA(1 * 3 + 0)
    val a11 = listA(1 * 3 + 1)
    val a12 = listA(1 * 3 + 2)
    val a20 = listA(2 * 3 + 0)
    val a21 = listA(2 * 3 + 1)
    val a22 = listA(2 * 3 + 2)

    val b00 = listB(0 * 3 + 0)
    val b01 = listB(0 * 3 + 1)
    val b02 = listB(0 * 3 + 2)
    val b10 = listB(1 * 3 + 0)
    val b11 = listB(1 * 3 + 1)
    val b12 = listB(1 * 3 + 2)
    val b20 = listB(2 * 3 + 0)
    val b21 = listB(2 * 3 + 1)
    val b22 = listB(2 * 3 + 2)

    Matrix3(
      a00 * b00 + a01 * b10 + a02 * b20,
      a00 * b01 + a01 * b11 + a02 * b21,
      a00 * b02 + a01 * b12 + a02 * b22,
      a10 * b00 + a11 * b10 + a12 * b20,
      a10 * b01 + a11 * b11 + a12 * b21,
      a10 * b02 + a11 * b12 + a12 * b22,
      a20 * b00 + a21 * b10 + a22 * b20,
      a20 * b01 + a21 * b11 + a22 * b21,
      a20 * b02 + a21 * b12 + a22 * b22
    )
  }

  def transpose(matrix3: Matrix3): Matrix3 = {
    val m = matrix3.mat
    Matrix3(
      m(0),
      m(3),
      m(6),
      m(1),
      m(4),
      m(7),
      m(2),
      m(5),
      m(8)
    )
  }

  def flip(horizontal: Boolean, vertical: Boolean): Matrix3 =
    (horizontal, vertical) match {
      case (true, true)   => Matrix3.scale(-1, -1)
      case (true, false)  => Matrix3.scale(-1, 1)
      case (false, true)  => Matrix3.scale(1, -1)
      case (false, false) => Matrix3.identity
    }

  def apply(): Matrix3 =
    identity

  def apply(
      row0: (Double, Double, Double),
      row1: (Double, Double, Double),
      row2: (Double, Double, Double)
  ): Matrix3 =
    Matrix3(
      List(row0._1, row0._2, row0._3, row1._1, row1._2, row1._3, row2._1, row2._2, row2._3)
    )

  def apply(
      a1: Double,
      a2: Double,
      a3: Double,
      b1: Double,
      b2: Double,
      b3: Double,
      c1: Double,
      c2: Double,
      c3: Double
  ): Matrix3 =
    Matrix3(
      List(
        a1,
        a2,
        a3,
        b1,
        b2,
        b3,
        c1,
        c2,
        c3
      )
    )

}
