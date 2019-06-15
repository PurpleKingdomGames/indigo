package indigo.platform.renderer

import indigo.shared.display.DisplayObject

import indigo.shared.EqualTo._

import org.scalajs.dom.raw.WebGLRenderingContext._

final class CompressedDisplayObject(val imageRef: String, val vertices: scalajs.js.Array[Double], val textureCoordinates: scalajs.js.Array[Double], val effectValues: scalajs.js.Array[Double]) {
  val vertexCount: Int = vertices.length / 3
  val mode: Int        = TRIANGLES
}
object CompressedDisplayObject {

  def sortAndCompress: List[DisplayObject] => List[CompressedDisplayObject] =
    sortByDepth andThen compress

  val sortByDepth: List[DisplayObject] => List[DisplayObject] = displayObjects => displayObjects.sortWith((d1, d2) => d1.z > d2.z)

  // Entirely for performance reasons
  def compress(displayObjects: List[DisplayObject]): List[CompressedDisplayObject] = {
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

      val newV = vertices(d)
      val newT = textureCoordinates(d)
      val newE = effectValues(d)

      if (imageRef === "") { // First pass, var is empty.
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      } else if (imageRef === d.imageRef) { // Same image? Pile on the values
        for (vv <- newV) v.push(vv)
        for (tt <- newT) t.push(tt)
        for (ee <- newE) e.push(ee)
      } else { // Otherwise, stash what you've got and start on the next one.
        res2 = new CompressedDisplayObject(imageRef, v, t, e) :: res2
        imageRef = d.imageRef
        v = newV
        t = newT
        e = newE
      }
    }

    new CompressedDisplayObject(imageRef, v, t, e) :: res2
  }

  def compressSingle(d: DisplayObject): CompressedDisplayObject =
    new CompressedDisplayObject(d.imageRef, vertices(d), textureCoordinates(d), effectValues(d))

  val vertices: DisplayObject => scalajs.js.Array[Double] =
    d => {
      val xd: Double = d.x.toDouble
      val yd: Double = d.y.toDouble
      val zd: Double = d.z.toDouble
      val wd: Double = d.width.toDouble
      val hd: Double = d.height.toDouble

      scalajs.js.Array[Double](
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
    }

  val textureCoordinates: DisplayObject => scalajs.js.Array[Double] =
    d => {
      val tx1 = if (d.flipHorizontal) 1 - d.frame.translate.x else d.frame.translate.x
      val tx2 = if (d.flipHorizontal) 1 - (d.frame.scale.x + d.frame.translate.x) else d.frame.scale.x + d.frame.translate.x
      val ty1 = if (d.flipVertical) 1 - d.frame.translate.y else d.frame.translate.y
      val ty2 = if (d.flipVertical) 1 - (d.frame.scale.y + d.frame.translate.y) else d.frame.scale.y + d.frame.translate.y

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
    }

  val effectValues: DisplayObject => scalajs.js.Array[Double] =
    d =>
      scalajs.js.Array[Double](
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha,
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha,
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha,
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha,
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha,
        d.tintR,
        d.tintG,
        d.tintB,
        d.alpha
      )

}
