package indigo.shared.display

import indigo.shared.AsString
import indigo.shared.EqualTo

final class DisplayObject(
    val x: Int,
    val y: Int,
    val z: Int,
    val width: Int,
    val height: Int,
    val rotation: Double,
    val scaleX: Double,
    val scaleY: Double,
    val imageRef: String,
    val alpha: Double,
    val tintR: Double,
    val tintG: Double,
    val tintB: Double,
    val flipHorizontal: Boolean,
    val flipVertical: Boolean,
    val frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
)
object DisplayObject {

  implicit val show: AsString[DisplayObject] = {
    val si = implicitly[AsString[Int]]
    val sd = implicitly[AsString[Double]]
    val ss = implicitly[AsString[String]]
    val sb = implicitly[AsString[Boolean]]
    val sf = implicitly[AsString[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets]]

    AsString.create { v =>
      val p: String =
        List(
          si.show(v.x),
          si.show(v.y),
          si.show(v.z),
          si.show(v.width),
          si.show(v.height),
          ss.show(v.imageRef),
          sd.show(v.alpha),
          sd.show(v.tintR),
          sd.show(v.tintG),
          sd.show(v.tintB),
          sb.show(v.flipHorizontal),
          sb.show(v.flipVertical),
          sf.show(v.frame)
        ).mkString(", ")

      s"DisplayObject($p)"
    }
  }

  implicit val eq: EqualTo[DisplayObject] = {
    val ei = implicitly[EqualTo[Int]]
    val ed = implicitly[EqualTo[Double]]
    val es = implicitly[EqualTo[String]]
    val eb = implicitly[EqualTo[Boolean]]
    val ef = implicitly[EqualTo[SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets]]

    EqualTo.create { (a, b) =>
      ei.equal(a.x, b.x) &&
      ei.equal(a.y, b.y) &&
      ei.equal(a.z, b.z) &&
      ei.equal(a.width, b.width) &&
      ei.equal(a.height, b.height) &&
      es.equal(a.imageRef, b.imageRef) &&
      ed.equal(a.alpha, b.alpha) &&
      ed.equal(a.tintR, b.tintR) &&
      ed.equal(a.tintG, b.tintG) &&
      ed.equal(a.tintB, b.tintB) &&
      eb.equal(a.flipHorizontal, b.flipHorizontal) &&
      eb.equal(a.flipVertical, b.flipVertical) &&
      ef.equal(a.frame, b.frame)
    }
  }

  def apply(
      x: Int,
      y: Int,
      z: Int,
      width: Int,
      height: Int,
      rotation: Double,
      scaleX: Double,
      scaleY: Double,
      imageRef: String,
      alpha: Double,
      tintR: Double,
      tintG: Double,
      tintB: Double,
      flipHorizontal: Boolean,
      flipVertical: Boolean,
      frame: SpriteSheetFrame.SpriteSheetFrameCoordinateOffsets
  ): DisplayObject =
    new DisplayObject(
      x,
      y,
      z,
      width,
      height,
      rotation,
      scaleX,
      scaleY,
      imageRef,
      alpha,
      tintR,
      tintG,
      tintB,
      flipHorizontal,
      flipVertical,
      frame
    )
}
