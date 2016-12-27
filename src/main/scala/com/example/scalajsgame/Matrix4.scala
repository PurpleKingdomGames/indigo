package com.example.scalajsgame

import scala.language.implicitConversions

case class Matrix4(mat: List[Double]) {

  def identity: Matrix4 = Matrix4.identity

  def translate(tx: Double, ty: Double, tz: Double): Matrix4 = {
    Matrix4.multiply(this, Matrix4.translation(tx, ty, tz))
  }

  def xRotate(angleInRadians: Double): Matrix4 = {
    Matrix4.multiply(this, Matrix4.xRotation(angleInRadians))
  }

  def yRotate(angleInRadians: Double): Matrix4 = {
    Matrix4.multiply(this, Matrix4.yRotation(angleInRadians))
  }

  def zRotate(angleInRadians: Double): Matrix4 = {
    Matrix4.multiply(this, Matrix4.zRotation(angleInRadians))
  }

  def scale(sx: Double, sy: Double, sz: Double): Matrix4 = {
    Matrix4.multiply(this, Matrix4.scaling(sx, sy, sz))
  }

}

case object Matrix4 {

  implicit def matrix4dToJsArray(mat4d: Matrix4): scalajs.js.Array[Double] = {
    val a = new scalajs.js.Array[Double]()
    mat4d.mat.foreach(d => a.push(d))
    a
  }

  def identity: Matrix4 = Matrix4(
    mat = List(
      1, 0, 0, 0,
      0, 1, 0, 0,
      0, 0, 1, 0,
      0, 0, 0, 1
    )
  )

  def projection(width: Double, height: Double, depth: Double): Matrix4 = {
    Matrix4(
      List(
        2 / width, 0,          0,         0,
        0,         2 / height, 0,         0,
        0,         0,          2 / depth, 0,
        0,         0,          0,         1
      )
    )
  }

  def orthographic(left: Double, right: Double, bottom: Double, top: Double, near: Double, far: Double): Matrix4 = {
    Matrix4(
      List(
        2 / (right - left),              0,                               0,                           0,
        0,                               2 / (top - bottom),              0,                           0,
        0,                               0,                               2 / (near - far),            0,
        (left + right) / (left - right), (bottom + top) / (bottom - top), (near + far) / (near - far), 1
      )
    )
  }

  def translation(tx: Double, ty: Double, tz: Double): Matrix4 = {
    Matrix4(
      List(
        1,  0,  0,  0,
        0,  1,  0,  0,
        0,  0,  1,  0,
        tx, ty, tz, 1
      )
    )
  }

  def xRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
        1, 0, 0, 0,
        0, c, s, 0,
        0, -s, c, 0,
        0, 0, 0, 1
      )
    )
  }

  def yRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
        c, 0, -s, 0,
        0, 1, 0, 0,
        s, 0, c, 0,
        0, 0, 0, 1
      )
    )
  }

  def zRotation(angleInRadians: Double): Matrix4 = {
    val c = Math.cos(angleInRadians)
    val s = Math.sin(angleInRadians)

    Matrix4(
      List(
        c, s, 0, 0,
        -s, c, 0, 0,
        0, 0, 1, 0,
        0, 0, 0, 1
      )
    )
  }

  def scaling(sx: Double, sy: Double, sz: Double): Matrix4 = {
    Matrix4(
      List(
        sx, 0,  0,  0,
        0, sy,  0,  0,
        0,  0, sz,  0,
        0,  0,  0,  1
      )
    )
  }

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
        b00 * a00 + b01 * a10 + b02 * a20 + b03 * a30,
        b00 * a01 + b01 * a11 + b02 * a21 + b03 * a31,
        b00 * a02 + b01 * a12 + b02 * a22 + b03 * a32,
        b00 * a03 + b01 * a13 + b02 * a23 + b03 * a33,
        b10 * a00 + b11 * a10 + b12 * a20 + b13 * a30,
        b10 * a01 + b11 * a11 + b12 * a21 + b13 * a31,
        b10 * a02 + b11 * a12 + b12 * a22 + b13 * a32,
        b10 * a03 + b11 * a13 + b12 * a23 + b13 * a33,
        b20 * a00 + b21 * a10 + b22 * a20 + b23 * a30,
        b20 * a01 + b21 * a11 + b22 * a21 + b23 * a31,
        b20 * a02 + b21 * a12 + b22 * a22 + b23 * a32,
        b20 * a03 + b21 * a13 + b22 * a23 + b23 * a33,
        b30 * a00 + b31 * a10 + b32 * a20 + b33 * a30,
        b30 * a01 + b31 * a11 + b32 * a21 + b33 * a31,
        b30 * a02 + b31 * a12 + b32 * a22 + b33 * a32,
        b30 * a03 + b31 * a13 + b32 * a23 + b33 * a33
      )
    )
  }

  def apply(): Matrix4 = identity

}