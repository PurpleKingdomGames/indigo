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

// TODO: Once batch rendering is in, almost all these fields can be private
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

  def vertices: scalajs.js.Array[Double] = DisplayObject.vertices //TODO: Transform

  //(v_texcoord * uTexcoordScale) + uTexcoordTranslate
  def textureCoordinates: scalajs.js.Array[Double] =
    DisplayObject.convertCoordsToJsArray(
      DisplayObject.transformTextureCoords(
        DisplayObject.textureCoordinates,
        frame.translate,
        frame.scale
      )
    )

  val effectValues: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha
  )
  val vertexCount: Int = DisplayObject.vertexCount
  val mode: Int = DisplayObject.mode

}

object DisplayObject {
  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    0,0,0,
    0,1,0,
    1,0,0,

    0,1,0,
    1,0,0,
    1,1,0
  )
  val vertexCount: Int = 6

  val textureCoordinates: List[Vector2] = List(
    Vector2(0, 0),
    Vector2(0, 1),
    Vector2(1, 0),

    Vector2(0, 1),
    Vector2(1, 0),
    Vector2(1, 1)
  )

  val mode: Int = TRIANGLES

  def transformTextureCoords(baseCoords: List[Vector2], translate: Vector2, scale: Vector2): List[Vector2] = {
    baseCoords.map(_.scale(scale).translate(translate))
  }

  def convertCoordsToJsArray(coords: List[Vector2]): scalajs.js.Array[Double] =
    coords.map(_.toScalaJSArrayDouble).foldLeft(scalajs.js.Array[Double]())(_.concat(_))

//  val matrix4: Matrix4 = Matrix4
//    .orthographic(0, cNc.width / magnification, cNc.height / magnification, 0, -10000, 10000)
//    .translate(displayObject.x, displayObject.y, displayObject.z)
//    .scale(displayObject.width, displayObject.height, 1)

  // then add the flip!
  //Matrix4.multiply(matrix4, flipMatrix((displayObject.flipHorizontal, displayObject.flipVertical)))

//  val flipMatrix: ((Boolean, Boolean)) => Matrix4 = flipValues => {
//    flipValues match {
//      case (true, true)   => Matrix4.identity.translate(1, 1, 0).scale(-1, -1, -1)
//      case (true, false)  => Matrix4.identity.translate(1, 0, 0).scale(-1,  1, -1)
//      case (false, true)  => Matrix4.identity.translate(0, 1, 0).scale( 1, -1, -1)
//      case (false, false) => Matrix4.identity
//    }
//  }

}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer)

