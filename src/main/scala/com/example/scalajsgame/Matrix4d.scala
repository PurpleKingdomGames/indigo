package com.example.scalajsgame

import scala.language.implicitConversions

case class Matrix4d(mat: List[Double]) {

  def identity: Matrix4d = Matrix4d.identity

  def perspective(fovy: Double, aspect: Double, zNear: Double, zFar: Double, zZeroToOne: Boolean): Matrix4d = {
    val h = Math.tan(fovy * 0.5)

    val farInf = zFar > 0 && zFar.isInfinite
    val nearInf = zNear > 0 && zNear.isInfinite

    val e = 1E-6

    val a: Double = 1.0 / (h * aspect)
    val b: Double = 1.0 / h
    val c: Double =
      if(farInf) 1E-6 - 1.0d
      else {
        if(nearInf)
          (if (zZeroToOne) 0.0 else 1.0) - e
        else {
          (if (zZeroToOne) zFar else zFar + zNear) / (zNear - zFar)
        }
      }
    val d: Double =
      if(farInf) (e - (if (zZeroToOne) 1.0 else 2.0)) * zNear
      else {
        if(nearInf)
          ((if (zZeroToOne) 1.0 else 2.0) - e) * zFar
        else {
          (if (zZeroToOne) zFar else zFar + zFar) * zNear / (zNear - zFar)
        }
      }

    Matrix4d(
      mat = List(
        a, 0, 0, 0,
        0, b, 0, 0,
        0, 0, c, -1.0,
        0, 0, 0, d
      )
    )
  }

  def translate(x: Double, y: Double, z: Double): Matrix4d = {
    Matrix4d(
      mat =
        (0 to 3).toList
          .flatMap(_ => List(x, y, z, 0f))
          .zip(mat)
          .map(p => p._1 + p._2)
    )
  }

}

case object Matrix4d {

  implicit def matrix4dToJsArray(mat4d: Matrix4d): scalajs.js.Array[Double] = {
    val a = new scalajs.js.Array[Double]()
    mat4d.mat.foreach(d => a.push(d))
    a
  }

  def identity: Matrix4d = Matrix4d(
    mat = List(
      1, 0, 0, 0,
      0, 1, 0, 0,
      0, 0, 1, 0,
      0, 0, 0, 1
    )
  )

  def apply(): Matrix4d = identity

}
