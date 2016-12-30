package purple.renderer

import org.scalajs.dom.html
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}

//sealed trait DisplayObject {
//  val x: Int
//  val y: Int
//  val width: Int
//  val height: Int
//  // TODO: Make this just be a path, and have the engine take care of the loading.
//  val image: html.Image //TODO: Maybe not an image, maybe just a colour? ADT?
//  val vertices: scalajs.js.Array[Double]
//  val textureCoordinates: scalajs.js.Array[Int]
//  val vertexCount: Int
//  val mode: Int //YUK! Wrap this in a real type?
//}

case class DisplayObject(x: Int, y: Int, width: Int, height: Int, imageRef: String, alpha: Double, tintR: Double, tintG: Double, tintB: Double)

//case class Triangle2D(x: Int, y: Int, width: Int, height: Int, image: html.Image) extends DisplayObject {
//  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
//    0,1,0,
//    0,0,0,
//    1,0,0
//  )
//  val vertexCount: Int = 3
//
//  val textureCoordinates: scalajs.js.Array[Int] = scalajs.js.Array[Int](
//    0,1,
//    0,0,
//    1,1
//  )
//
//  val mode: Int = TRIANGLES
//}

//case class Rectangle2D(x: Int, y: Int, width: Int, height: Int, image: html.Image) extends DisplayObject {
//
//  /*
//  B--D
//  |\ |
//  | \|
//  A--C
//   */
//  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
//    0,0,0,
//    0,1,0,
//    1,0,0,
//    1,1,0
//  )
//  val vertexCount: Int = 4
//
//  val textureCoordinates: scalajs.js.Array[Int] = scalajs.js.Array[Int](
//    0,0,
//    0,1,
//    1,0,
//    1,1
//  )
//
//  val mode: Int = TRIANGLE_STRIP
//}

object Rectangle2D {
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,0,0,
    0,1,0,
    1,0,0,
    1,1,0
  )
  val vertexCount: Int = 4

  val textureCoordinates: scalajs.js.Array[Int] = scalajs.js.Array[Int](
    0,0,
    0,1,
    1,0,
    1,1
  )

  val mode: Int = TRIANGLE_STRIP
}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer)

