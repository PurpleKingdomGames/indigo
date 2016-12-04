package com.example.scalajsgame
/*
val mat = List(
1,2,3,4,
1,2,3,4,
1,2,3,4,
1,2,3,4
)

val mat2 = List(
4,4,4,4,
3,3,3,3,
2,2,2,2,
1,1,1,1
)

object Matrix4x4 {

  def fromNumericList[T](list: List[T])(implicit num: Numeric[T]): Option[Matrix4x4[T]] = {
    if(mat.size == 16) Some(Matrix4x4(list))
    else None
  }

  case class Matrix4x4[T](mat: List[T]) {
    val m00 = mat(0)
    val m01 = mat(1)
    val m02 = mat(2)
    val m03 = mat(3)
    val m10 = mat(4)
    val m11 = mat(5)
    val m12 = mat(6)
    val m13 = mat(7)
    val m20 = mat(8)
    val m21 = mat(9)
    val m22 = mat(10)
    val m23 = mat(11)
    val m30 = mat(12)
    val m31 = mat(13)
    val m32 = mat(14)
    val m33 = mat(15)

    def transpose: Matrix4x4[T] = {
      Matrix4x4(
        List(
          m03, m13, m23, m33,
          m02, m12, m22, m32,
          m01, m11, m21, m31,
          m00, m10, m20, m30
        )
      )
    }

    def multiply(that: Matrix4x4[T])(implicit num: Numeric[T]): Matrix4x4[T] = {
      val t = that.transpose
      //tl: first row first column (from 2x2) so r0c0*r0c0+r0c1*r1c0
      Matrix4x4(
        List(
          x, x, x, x,
          x, x, x, x,
          x, x, x, x,
          x, x, x, x
        )
      )
    }
  }
}

Matrix4x4.fromNumericList(mat).get.transpose == Matrix4x4.fromNumericList(mat2).get

val m2 = Matrix4x4.fromNumericList(List(2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2)).get

val m8 = Matrix4x4.fromNumericList(List(8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8)).get

m2.multiply(m2)
m2.multiply(m2) == m8
*/