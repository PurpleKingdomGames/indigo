package com.purplekingdomgames.indigo.renderer

import com.purplekingdomgames.indigo.gameengine.scenegraph.datatypes.AmbientLight
import org.scalajs.dom.raw.WebGLRenderingContext._

case class Displayable(game: GameDisplayLayer, lighting: LightingDisplayLayer, ui: UiDisplayLayer)
case class GameDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
case class LightingDisplayLayer(displayObjects: List[DisplayObject], ambientLight: AmbientLight) extends DisplayLayer
case class UiDisplayLayer(displayObjects: List[DisplayObject]) extends DisplayLayer
sealed trait DisplayLayer {
  val displayObjects: List[DisplayObject]
}

case class CompressedDisplayObject(imageRef: String, vertices: scalajs.js.Array[Double], textureCoordinates: scalajs.js.Array[Double], effectValues: scalajs.js.Array[Double]) {
  val vertexCount: Int = vertices.length / 3
  val mode: Int = TRIANGLES
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

  private val flipTH: Int = if(flipHorizontal) 1 else 0
  private val flipTV: Int = if(flipVertical) 1 else 0
  private val flipSH: Int = if(flipHorizontal) -1 else 1
  private val flipSV: Int = if(flipVertical) -1 else 1

  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    x,         y,          z,
    x,         height + y, z,
    width + x, y,          z,

    x,         height + y, z,
    width + x, y,          z,
    width + x, height + y, z
  )

  private val tx1 = if(flipHorizontal) 1 - frame.translate.x else frame.translate.x
  private val tx2 = if(flipHorizontal) 1 - (frame.scale.x + frame.translate.x) else frame.scale.x + frame.translate.x
  private val ty1 = if(flipVertical) 1 - frame.translate.y else frame.translate.y
  private val ty2 = if(flipVertical) 1 - (frame.scale.y + frame.translate.y) else frame.scale.y + frame.translate.y

  val textureCoordinates: scalajs.js.Array[Double] =
    scalajs.js.Array[Double](
      tx1, ty1,
      tx1, ty2,
      tx2, ty1,

      tx1, ty2,
      tx2, ty1,
      tx2, ty2
    )

  private val effectValues: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha,
    tintR,tintG,tintB,alpha
  )

  def toCompressed: CompressedDisplayObject =
    CompressedDisplayObject(imageRef, vertices, textureCoordinates, effectValues)

}

object DisplayObject {

  def sortAndCompress: List[DisplayObject] => List[CompressedDisplayObject] =
    sortByDepth andThen compress

  val sortByDepth: List[DisplayObject] => List[DisplayObject] = displayObjects => {
    displayObjects.sortWith((d1, d2) => d1.z < d2.z)
  }

  // Entirely for performance reasons
  def compress: List[DisplayObject] => List[CompressedDisplayObject] = displayObjects => {
    var imageRef: String = ""
    var v: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    var t: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    var e: scalajs.js.Array[Double] = scalajs.js.Array[Double]()

    var res2: List[CompressedDisplayObject] = Nil

    for(d <- displayObjects) {

      val newV = d.vertices
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
        res2 = CompressedDisplayObject(imageRef, v, t, e) :: res2
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      }
    }

    CompressedDisplayObject(imageRef, v, t, e) :: res2
  }

}
