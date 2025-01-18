package indigo.shared.datatypes

import util.control.Breaks.*

opaque type Matrix4 = Array[Double]

object Matrix4:

  extension (m: Matrix4)
    def toArray: Array[Double] =
      m

    inline def row1: Array[Double] = Array(m(0), m(1), m(2), m(3))
    inline def row2: Array[Double] = Array(m(4), m(5), m(6), m(7))
    inline def row3: Array[Double] = Array(m(8), m(9), m(10), m(11))
    inline def row4: Array[Double] = Array(m(12), m(13), m(14), m(15))
    inline def col1: Array[Double] = Array(m(0), m(4), m(8), m(12))
    inline def col2: Array[Double] = Array(m(1), m(5), m(9), m(13))
    inline def col3: Array[Double] = Array(m(2), m(6), m(10), m(14))
    inline def col4: Array[Double] = Array(m(3), m(7), m(11), m(15))
    inline def x: Double           = m(12)
    inline def y: Double           = m(13)
    inline def z: Double           = m(14)
    inline def data: (Array[Double], Array[Double]) =
      (Array(m(0), m(1), m(4), m(5)), Array(m(12), m(13), m(14)))

    def translate(by: Vector3): Matrix4 =
      translate(by.x, by.y, by.z)
    def translate(byX: Double, byY: Double, byZ: Double): Matrix4 =
      m * Matrix4(
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
        byX,
        byY,
        byZ,
        1
      )

    def rotate(angle: Radians): Matrix4 =
      val c = Math.cos(angle.toDouble)
      val s = Math.sin(angle.toDouble)

      m * Matrix4(
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

    def scale(by: Vector2): Matrix4 =
      scale(by.x, by.y, 1.0d)
    def scale(by: Vector3): Matrix4 =
      scale(by.x, by.y, by.z)
    def scale(byX: Double, byY: Double, byZ: Double): Matrix4 =
      m * Matrix4(
        byX,
        0,
        0,
        0,
        0,
        byY,
        0,
        0,
        0,
        0,
        byZ,
        0,
        0,
        0,
        0,
        1
      )

    def transpose: Matrix4 =
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

    def *(other: Matrix4): Matrix4 =
      val listA = m.toArray
      val listB = other.toArray

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

    @SuppressWarnings(Array("scalafix:DisableSyntax.var", "scalafix:DisableSyntax.while"))
    def ~==(other: Matrix4): Boolean =
      if (m.length == other.toArray.length) {
        var count = m.length - 1
        var same  = true
        while (count > 0) {
          breakable {
            if (Math.abs(m(count) - other.toArray(count)) > 0.001)
              same = false
            break()
          }
          count = count - 1
        }
        same
      } else false

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

  /** Usage Matrix.projection(2 * aspectRatio, 2, 2) (assuming width > height) because the screen by default is 2 x 2
    * units: -1 to 1
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
    orthographic(0, width, height, 0, -1, Int.MaxValue.toDouble)

  inline def apply(): Matrix4 = identity
  inline def apply(matrix: Array[Double]): Matrix4 =
    matrix

  inline def apply(
      row0: (Double, Double, Double, Double),
      row1: (Double, Double, Double, Double),
      row2: (Double, Double, Double, Double),
      row3: (Double, Double, Double, Double)
  ): Matrix4 =
    Matrix4(
      Array(row0._1, row0._2, row0._3, row0._4) ++
        Array(row1._1, row1._2, row1._3, row1._4) ++
        Array(row2._1, row2._2, row2._3, row2._4) ++
        Array(row3._1, row3._2, row3._3, row3._4)
    )

  inline def apply(
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
      Array(
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
end Matrix4
