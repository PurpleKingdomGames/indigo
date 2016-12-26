package com.example.scalajsgame

import org.scalajs.dom.html
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}
import org.scalajs.dom.raw.WebGLRenderingContext._

sealed trait DisplayObject {
  val x: Int
  val y: Int
  val image: html.Image
  val vertices: scalajs.js.Array[Double]
  val textureCoordinates: scalajs.js.Array[Int]
  val vertexCount: Int
  val mode: Int //YUK! Wrap this in a real type?
}

case class Triangle2D(x: Int, y: Int, image: html.Image) extends DisplayObject {
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,1,0,
    0,0,0,
    1,0,0
  )
  val vertexCount: Int = 3

  val textureCoordinates: scalajs.js.Array[Int] = scalajs.js.Array[Int](
    0,1,
    0,0,
    1,1
  )

  val mode: Int = TRIANGLES
}

case class Rectangle2D(x: Int, y: Int, image: html.Image) extends DisplayObject {

  /*
  B--D
  |\ |
  | \|
  A--C
   */
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,0,0,
    0,1,0,
    1,0,0,
    1,1,0
  )
  val vertexCount: Int = 4

  val textureCoordinates: scalajs.js.Array[Int] = scalajs.js.Array[Int](
    0,1,
    0,0,
    1,1,
    1,0
  )

  val mode: Int = TRIANGLE_STRIP
}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer)

