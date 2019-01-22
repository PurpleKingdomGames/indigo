package indigo.renderer

import indigo.gameengine.scenegraph.datatypes.AmbientLight
import org.scalajs.dom.raw.WebGLRenderingContext._

import indigo.Eq._

final case class Displayable(game: DisplayLayer, lighting: DisplayLayer, ui: DisplayLayer, ambientLight: AmbientLight)
final case class DisplayLayer(displayObjects: List[DisplayObject]) extends AnyVal

final case class CompressedDisplayObject(imageRef: String, vertices: scalajs.js.Array[Double], textureCoordinates: scalajs.js.Array[Double], effectValues: scalajs.js.Array[Double]) {
  val vertexCount: Int = vertices.length / 3
  val mode: Int        = TRIANGLES
}

final case class DisplayObject(x: Int,
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
                               frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets) {

  val xd: Double = x.toDouble
  val yd: Double = y.toDouble
  val zd: Double = z.toDouble
  val wd: Double = width.toDouble
  val hd: Double = height.toDouble

  val vertices: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    xd,
    yd,
    zd,
    xd,
    hd + yd,
    zd,
    wd + xd,
    yd,
    zd,
    xd,
    hd + yd,
    zd,
    wd + xd,
    yd,
    zd,
    wd + xd,
    hd + yd,
    zd
  )

  private val tx1 = if (flipHorizontal) 1 - frame.translate.x else frame.translate.x
  private val tx2 = if (flipHorizontal) 1 - (frame.scale.x + frame.translate.x) else frame.scale.x + frame.translate.x
  private val ty1 = if (flipVertical) 1 - frame.translate.y else frame.translate.y
  private val ty2 = if (flipVertical) 1 - (frame.scale.y + frame.translate.y) else frame.scale.y + frame.translate.y

  val textureCoordinates: scalajs.js.Array[Double] =
    scalajs.js.Array[Double](
      tx1,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx1,
      ty2,
      tx2,
      ty1,
      tx2,
      ty2
    )

  private val effectValues: scalajs.js.Array[Double] = scalajs.js.Array[Double](
    tintR,
    tintG,
    tintB,
    alpha,
    tintR,
    tintG,
    tintB,
    alpha,
    tintR,
    tintG,
    tintB,
    alpha,
    tintR,
    tintG,
    tintB,
    alpha,
    tintR,
    tintG,
    tintB,
    alpha,
    tintR,
    tintG,
    tintB,
    alpha
  )

  def toCompressed: CompressedDisplayObject =
    CompressedDisplayObject(imageRef, vertices, textureCoordinates, effectValues)

}

object DisplayObject {

  def sortAndCompress: List[DisplayObject] => List[CompressedDisplayObject] =
    sortByDepth andThen compress

  val sortByDepth: List[DisplayObject] => List[DisplayObject] = displayObjects => displayObjects.sortWith((d1, d2) => d1.z > d2.z)

  // Entirely for performance reasons
  def compress: List[DisplayObject] => List[CompressedDisplayObject] = displayObjects => {
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var imageRef: String = ""
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var v: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var t: scalajs.js.Array[Double] = scalajs.js.Array[Double]()
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var e: scalajs.js.Array[Double] = scalajs.js.Array[Double]()

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    var res2: List[CompressedDisplayObject] = Nil

    for (d <- displayObjects) {

      val newV = d.vertices
      val newT = d.textureCoordinates
      val newE = d.effectValues

      if (imageRef === "") {
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      } else if (imageRef === d.imageRef) {
        for (vv <- newV) v.push(vv)
        for (tt <- newT) t.push(tt)
        for (ee <- newE) e.push(ee)
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
