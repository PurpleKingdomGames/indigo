package indigo.shared.datatypes.mutable

import indigo.shared.datatypes.Matrix4

final case class CheapMatrix4(mat: Array[Double]) {

  lazy val x: Double = mat(12)
  lazy val y: Double = mat(13)

  lazy val data: (List[Double], List[Double]) =
    (List(mat(0), mat(1), mat(4), mat(5)), List(mat(12), mat(13), mat(14)))

  def translate(byX: Double, byY: Double, byZ: Double): CheapMatrix4 =
    this * CheapMatrix4.translate(byX, byY, byZ)

  def rotate(angle: Double): CheapMatrix4 =
    this * CheapMatrix4.rotation(angle)

  def scale(byX: Double, byY: Double, byZ: Double): CheapMatrix4 =
    this * CheapMatrix4.scale(byX, byY, byZ)

  def *(other: CheapMatrix4): CheapMatrix4 =
    *(other.mat)

  def *(other: Array[Double]): CheapMatrix4 = {

    // If they are commented out below, it's because we know...
    // ... that those fields aren't used by the engine...
    // ... and by knowing things we can avoid work.
    
    val listA = mat
    val listB = other

    val a00 = listA(0 * 4 + 0)
    val a01 = listA(0 * 4 + 1)
    val a02 = listA(0 * 4 + 2)
    val a03 = listA(0 * 4 + 3)
    val a10 = listA(1 * 4 + 0)
    val a11 = listA(1 * 4 + 1)
    val a12 = listA(1 * 4 + 2)
    val a13 = listA(1 * 4 + 3)
    // val a20 = listA(2 * 4 + 0)
    // val a21 = listA(2 * 4 + 1)
    // val a22 = listA(2 * 4 + 2)
    // val a23 = listA(2 * 4 + 3)
    val a30 = listA(3 * 4 + 0)
    val a31 = listA(3 * 4 + 1)
    val a32 = listA(3 * 4 + 2)
    val a33 = listA(3 * 4 + 3)

    val b00 = listB(0 * 4 + 0)
    val b01 = listB(0 * 4 + 1)
    val b02 = listB(0 * 4 + 2)
    // val b03 = listB(0 * 4 + 3)
    val b10 = listB(1 * 4 + 0)
    val b11 = listB(1 * 4 + 1)
    val b12 = listB(1 * 4 + 2)
    // val b13 = listB(1 * 4 + 3)
    val b20 = listB(2 * 4 + 0)
    val b21 = listB(2 * 4 + 1)
    val b22 = listB(2 * 4 + 2)
    // val b23 = listB(2 * 4 + 3)
    val b30 = listB(3 * 4 + 0)
    val b31 = listB(3 * 4 + 1)
    val b32 = listB(3 * 4 + 2)
    // val b33 = listB(3 * 4 + 3)

    mat(0) = a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30
    mat(1) = a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31
    mat(2) = a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32
    //mat(3) = a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33

    mat(4) = a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30
    mat(5) = a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31
    mat(6) = a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32
    //mat(7) = a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33

    // mat(8) = a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30
    // mat(9) = a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31
    // mat(10) = a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32
    //mat(11) = a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33

    mat(12) = a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30
    mat(13) = a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31
    mat(14) = a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32
    //mat(15) = a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33

    this
  }

  def toArray: Array[Double] =
    mat

  def toMatrix4: Matrix4 =
    Matrix4(mat.toList)

}

object CheapMatrix4 {

  def identity: CheapMatrix4 =
    CheapMatrix4(
      (1, 0, 0, 0),
      (0, 1, 0, 0),
      (0, 0, 1, 0),
      (0, 0, 0, 1)
    )

  def orthographic(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double): CheapMatrix4 =
    CheapMatrix4(
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

  def orthographic(width: Double, height: Double): CheapMatrix4 =
    orthographic(0, width, height, 0, -10000, 10000)

  def translate(tx: Double, ty: Double, tz: Double): Array[Double] =
    Array(
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

  def rotation(angleInRadians: Double): Array[Double] = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Array(
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

  def scale(sx: Double, sy: Double, sz: Double): Array[Double] =
    Array(
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

  def apply(
      row0: (Double, Double, Double, Double),
      row1: (Double, Double, Double, Double),
      row2: (Double, Double, Double, Double),
      row3: (Double, Double, Double, Double)
  ): CheapMatrix4 =
    CheapMatrix4(
      Array(row0._1, row0._2, row0._3, row0._4) ++
        Array(row1._1, row1._2, row1._3, row1._4) ++
        Array(row2._1, row2._2, row2._3, row2._4) ++
        Array(row3._1, row3._2, row3._3, row3._4)
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
  ): CheapMatrix4 =
    CheapMatrix4(
      Array[Double](
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
