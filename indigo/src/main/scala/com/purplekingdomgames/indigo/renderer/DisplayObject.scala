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

case class CompressedDisplayObject(imageRef: String, orthographicProjectionMatrix: Matrix4, vertices: scalajs.js.Array[Double], textureCoordinates: scalajs.js.Array[Double], effectValues: scalajs.js.Array[Double]) {
  val vertexCount: Int = vertices.length / 3
  val mode: Int = DisplayObject.mode

  def +(other: CompressedDisplayObject): CompressedDisplayObject =
    CompressedDisplayObject(
      imageRef,
      orthographicProjectionMatrix,
      vertices.concat(other.vertices),
      textureCoordinates.concat(other.textureCoordinates),
      effectValues.concat(other.effectValues)
    )

  def addDisplayObject(displayObject: DisplayObject): CompressedDisplayObject =
    this + displayObject.toCompressed(orthographicProjectionMatrix)
}

// TODO: Once batch rendering is in, almost all these fields can be private
// TODO: Some of this can definitely be optimised, if there's any benefit to that.
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

  private def vertices(orthographicProjectionMatrix: Matrix4): scalajs.js.Array[Double] =
    DisplayObject.convertVertexCoordsToJsArray(
      DisplayObject.transformVertexCoords(
        DisplayObject.vertices,
        Matrix4
          .translateAndScale(x, y, z, width, height, 1)
          .withOrthographic(orthographicProjectionMatrix)
          .flip(flipHorizontal, flipVertical)
      )
    )

  private def textureCoordinates: scalajs.js.Array[Double] =
    DisplayObject.convertTextureCoordsToJsArray(
      DisplayObject.transformTextureCoords(
        DisplayObject.textureCoordinates,
        frame.translate,
        frame.scale
      )
    )

  private val effectValues: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha
  )

  def toCompressed(orthographicProjectionMatrix: Matrix4): CompressedDisplayObject =
    CompressedDisplayObject(imageRef, orthographicProjectionMatrix, vertices(orthographicProjectionMatrix), textureCoordinates, effectValues)

}

object DisplayObject {

  val vertices: List[Vector4] = List(
    Vector4.position(0, 0, 0),
    Vector4.position(0, 1, 0),
    Vector4.position(1, 0, 0),

    Vector4.position(0, 1, 0),
    Vector4.position(1, 0, 0),
    Vector4.position(1, 1, 0)
  )

  val vertexCount: Int = 6

  def transformVertexCoords(baseCoords: List[Vector4], matrix4: Matrix4): List[Vector4] = {
    baseCoords.map(_.applyMatrix4(matrix4))
  }

  def convertVertexCoordsToJsArray(coords: List[Vector4]): scalajs.js.Array[Double] =
    coords.map(_.toScalaJSArrayDouble).foldLeft(scalajs.js.Array[Double]())(_.concat(_))

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

  def convertTextureCoordsToJsArray(coords: List[Vector2]): scalajs.js.Array[Double] =
    coords.map(_.toScalaJSArrayDouble).foldLeft(scalajs.js.Array[Double]())(_.concat(_))

  def sortAndCompress(orthographicProjectionMatrix: Matrix4): List[DisplayObject] => List[CompressedDisplayObject] =
    sortByDepth andThen compress(orthographicProjectionMatrix)

  val sortByDepth: List[DisplayObject] => List[DisplayObject] = displayObjects => {
    displayObjects.sortWith((d1, d2) => d1.z < d2.z)
  }

  // Entirely for performance reasons and it's still too slow...
  def compress(orthographicProjectionMatrix: Matrix4): List[DisplayObject] => List[CompressedDisplayObject] = displayObjects => {
    var imageRef: String = ""
    var v: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    var t: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    var e: scalajs.js.Array[Double] = scalajs.js.Array[Double]()

    var res2: List[CompressedDisplayObject] = Nil

    for(d <- displayObjects) {

      val newV = d.vertices(orthographicProjectionMatrix)
      val newT = d.textureCoordinates
      val newE = d.effectValues

      if(imageRef == "") {
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      } else if(imageRef == d.imageRef) {
        for(vv <- newV) { v.push(vv) }
        for(tt <- newT) { t.push(tt) }
        for(ee <- newE) { e.push(ee) }
      } else {
        res2 = CompressedDisplayObject(imageRef, orthographicProjectionMatrix, v, t, e) :: res2
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      }
    }

    CompressedDisplayObject(imageRef, orthographicProjectionMatrix, v, t, e) :: res2
  }

}

case class RenderableThing(displayObject: DisplayObject, shaderProgram: WebGLProgram, vertexBuffer: WebGLBuffer, textureBuffer: WebGLBuffer)

