package indigo.shared.datatypes.mutable

import indigo.shared.datatypes.Matrix4
import indigo.shared.datatypes.Radians
import indigo.shared.datatypes.Vector3

/** `CheapMatrix4` is intended for use internally within Indigo, but remains available for general use. You are advised
  * to use `Matrix4` generally. `CheapMatrix4` carries over much of the functionality of `Matrix4` but is based on
  * mutable data for performance reasons, and takes some shortcuts during multiplication to reduce work based on how the
  * engine itself behaves.
  */
final case class CheapMatrix4(mat: Array[Float]) derives CanEqual {

  lazy val x: Float = mat(12)
  lazy val y: Float = mat(13)

  lazy val data: (List[Float], List[Float]) =
    (List(mat(0), mat(1), mat(4), mat(5)), List(mat(12), mat(13), mat(14)))

  def translate(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
    this * CheapMatrix4.translate(byX, byY, byZ)

  def rotate(angle: Float): CheapMatrix4 =
    this * CheapMatrix4.rotation(angle)

  def scale(byX: Float, byY: Float, byZ: Float): CheapMatrix4 =
    this * CheapMatrix4.scale(byX, byY, byZ)

  def *(other: CheapMatrix4): CheapMatrix4 =
    *(other.mat)

  def *(other: Array[Float]): CheapMatrix4 = {

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

  def toArray: Array[Float] =
    mat

  def toMatrix4: Matrix4 =
    Matrix4(mat.toList.map(_.toDouble))

  def deepClone: CheapMatrix4 =
    CheapMatrix4(Array[Float]().concat(mat))

  def transform(vector: Vector3): Vector3 =
    val col1: List[Float] = List(mat(0), mat(4), mat(8), mat(12))
    val col2: List[Float] = List(mat(1), mat(5), mat(9), mat(13))
    val col3: List[Float] = List(mat(2), mat(6), mat(10), mat(14))

    Vector3(
      x = col1(0) * vector.x + col1(1) * vector.y + col1(2) * vector.z + col1(3),
      y = col2(0) * vector.x + col2(1) * vector.y + col2(2) * vector.z + col2(3),
      z = col3(0) * vector.x + col3(1) * vector.y + col3(2) * vector.z + col3(3)
    )
}

object CheapMatrix4 {

  def identity: CheapMatrix4 =
    CheapMatrix4(
      Array(1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)
    )

  def orthographic(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float): CheapMatrix4 =
    CheapMatrix4(
      Array(
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

  def orthographic(width: Float, height: Float): CheapMatrix4 =
    orthographic(0, width, height, 0, -10000, 10000)

  def orthographic(x: Float, y: Float, width: Float, height: Float): CheapMatrix4 =
    orthographic(x, x + width, y + height, y, -10000, 10000)

  def translate(tx: Float, ty: Float, tz: Float): Array[Float] =
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

  def rotation(angleInRadians: Float): Array[Float] = {
    val c = Math.cos(angleInRadians).toFloat
    val s = Math.sin(angleInRadians).toFloat

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

  def scale(sx: Float, sy: Float, sz: Float): Array[Float] =
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

  /** SHOULD ONLY BE USED BY TESTS
    */
  def apply(
      row0: (Float, Float, Float, Float),
      row1: (Float, Float, Float, Float),
      row2: (Float, Float, Float, Float),
      row3: (Float, Float, Float, Float)
  ): CheapMatrix4 =
    CheapMatrix4(
      Array(row0._1, row0._2, row0._3, row0._4) ++
        Array(row1._1, row1._2, row1._3, row1._4) ++
        Array(row2._1, row2._2, row2._3, row2._4) ++
        Array(row3._1, row3._2, row3._3, row3._4)
    )
}
