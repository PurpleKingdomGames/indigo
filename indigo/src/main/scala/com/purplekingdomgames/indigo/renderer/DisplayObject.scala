package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AmbientLight
import org.scalajs.dom.raw.WebGLRenderingContext._
import org.scalajs.dom.raw.{WebGLBuffer, WebGLProgram}

case class Displayable(game: GameDisplayLayer, lighting: LightingDisplayLayer, ui: UiDisplayLayer)
case class GameDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
case class LightingDisplayLayer(displayObjects: List[DisplayObject], ambientLight: AmbientLight) extends DisplayLayer
case class UiDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
sealed trait DisplayLayer {
  val displayObjects: List[DisplayObject]
}

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
                         flipVertical: Boolean,
                         frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
                        ) {

  val vertices: scalajs.js.Array[Double] = DisplayObject.vertices
  val textureCoordinates: scalajs.js.Array[Int] = DisplayObject.textureCoordinates
  val vertexCount: Int = DisplayObject.vertexCount
  val mode: Int = DisplayObject.mode

}

object DisplayObject {
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

