package purple.renderer

import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}

case class DisplayObject(x: Int,
                         y: Int,
                         z: Int,
                         width: Int,
                         height: Int,
                         imageRef: String,
                         alpha: Double,
                         tintR: Double,
                         tintG: Double,
                         tintB: Double,
                         flipHorizontal: Boolean,
                         flipVertical: Boolean)

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

