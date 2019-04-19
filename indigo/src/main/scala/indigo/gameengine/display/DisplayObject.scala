package indigo.gameengine.display

import indigo.gameengine.scenegraph.datatypes.AmbientLight
import indigo.renderer.SpriteSheetFrame

import indigo.shared.AsString
import indigo.shared.EqualTo

final class Displayable(val game: DisplayLayer, val lighting: DisplayLayer, val ui: DisplayLayer, val ambientLight: AmbientLight)
object Displayable {

  implicit val show: AsString[Displayable] = {
    val sd = implicitly[AsString[DisplayLayer]]
    val sa = implicitly[AsString[AmbientLight]]

    AsString.create { v =>
      s"Displayable(${sd.show(v.game)}, ${sd.show(v.lighting)}, ${sd.show(v.ui)}, ${sa.show(v.ambientLight)})"
    }
  }

  implicit val eq: EqualTo[Displayable] = {
    val ed = implicitly[EqualTo[DisplayLayer]]
    val ea = implicitly[EqualTo[AmbientLight]]

    EqualTo.create { (a, b) =>
      ed.equal(a.game, b.game) &&
      ed.equal(a.lighting, b.lighting) &&
      ed.equal(a.ui, b.ui) &&
      ea.equal(a.ambientLight, b.ambientLight)
    }
  }

  def apply(game: DisplayLayer, lighting: DisplayLayer, ui: DisplayLayer, ambientLight: AmbientLight): Displayable =
    new Displayable(game, lighting, ui, ambientLight)
}

final class DisplayLayer(val displayObjects: List[DisplayObject]) extends AnyVal
object DisplayLayer {

  implicit val show: AsString[DisplayLayer] = {
    val ev = implicitly[AsString[List[DisplayObject]]]

    AsString.create { v =>
      s"DisplayLayer(${ev.show(v.displayObjects)})"
    }
  }

  implicit val eq: EqualTo[DisplayLayer] = {
    val ev = implicitly[EqualTo[List[DisplayObject]]]

    EqualTo.create { (a, b) =>
      ev.equal(a.displayObjects, b.displayObjects)
    }
  }

  def apply(displayObjects: List[DisplayObject]): DisplayLayer =
    new DisplayLayer(displayObjects)
}

final class DisplayObject(
    val x: Int,
    val y: Int,
    val z: Int,
    val width: Int,
    val height: Int,
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
