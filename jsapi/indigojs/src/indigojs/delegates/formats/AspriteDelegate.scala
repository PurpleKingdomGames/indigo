package indigojs.delegates.formats

import indigo.shared.formats._
import scala.scalajs.js.annotation._

import indigojs.delegates._
import scala.scalajs.js.JSConverters._
import scala.scalajs.js

import indigojs.delegates.SceneGraphNodeUtilities._
import indigojs.delegates.AnimationUtilities._
import indigo.shared.assets.AssetName
import indigo.shared.formats.Aseprite
import indigo.shared.formats.SpriteAndAnimations

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("Aseprite")
final case class AsepriteDelegate(_frames: List[AsepriteFrameDelegate], _meta: AsepriteMetaDelegate) {
  @JSExport
  val frames = _frames;
  @JSExport
  val meta = _meta;

  def toInternal: Aseprite =
    Aseprite(frames.map(_.toInternal), meta.toInternal)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AsepriteFrame")
final case class AsepriteFrameDelegate(
    _filename: String,
    _frame: AsepriteRectangleDelegate,
    _rotated: Boolean,
    _trimmed: Boolean,
    _spriteSourceSize: AsepriteRectangleDelegate,
    _sourceSize: AsepriteSizeDelegate,
    _duration: Int
) {
  @JSExport
  val filename = _filename;
  @JSExport
  val frame = _frame;
  @JSExport
  val rotated = _rotated;
  @JSExport
  val trimmed = _trimmed;
  @JSExport
  val spriteSourceSize = _spriteSourceSize;
  @JSExport
  val sourceSize = _sourceSize;
  @JSExport
  val duration = _duration;

  def toInternal: AsepriteFrame =
    AsepriteFrame(filename, frame.toInternal, rotated, trimmed, spriteSourceSize.toInternal, sourceSize.toInternal, duration)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AsepriteRectangle")
final case class AsepriteRectangleDelegate(_x: Int, _y: Int, _w: Int, _h: Int) {
  @JSExport
  val x = _x;
  @JSExport
  val y = _y;
  @JSExport
  val w = _w;
  @JSExport
  val h = _h;

  def toInternal: AsepriteRectangle =
    AsepriteRectangle(x, y, w, h)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AsepriteMeta")
final case class AsepriteMetaDelegate(_app: String, _version: String, _image: String, _format: String, _size: AsepriteSizeDelegate, _scale: String, _frameTags: List[AsepriteFrameTagDelegate]) {
  @JSExport
  val app = _app;
  @JSExport
  val version = _version;
  @JSExport
  val image = _image;
  @JSExport
  val format = _format;
  @JSExport
  val size = _size;
  @JSExport
  val scale = _scale;
  @JSExport
  val frameTags = _frameTags;

  def toInternal: AsepriteMeta =
    AsepriteMeta(app, version, image, format, size.toInternal, scale, frameTags.map(_.toInternal))
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AsepriteSize")
final case class AsepriteSizeDelegate(_w: Int, _h: Int) {
  @JSExport
  val w = _w;
  @JSExport
  val h = _h;

  def toInternal: AsepriteSize =
    AsepriteSize(w, h)
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
@JSExportTopLevel("AsepriteFrameTag")
final case class AsepriteFrameTagDelegate(_name: String, _from: Int, _to: Int, _direction: String) {
  @JSExport
  val name = _name;
  @JSExport
  val from = _from;
  @JSExport
  val to = _to;
  @JSExport
  val direction = _direction;

  def toInternal: AsepriteFrameTag =
    AsepriteFrameTag(name, from, to, direction)
}

final object AsepriteUtilities {
  implicit class AsepriteConvert(val obj: Aseprite) {
    def toJsDelegate =
      AsepriteDelegate(obj.frames.map(_.toJsDelegate), obj.meta.toJsDelegate)
  }

  implicit class AsepriteFrameConvert(val obj: AsepriteFrame) {
    def toJsDelegate =
      AsepriteFrameDelegate(obj.filename, obj.frame.toJsDelegate, obj.rotated, obj.trimmed, obj.spriteSourceSize.toJsDelegate, obj.sourceSize.toJsDelegate, obj.duration)
  }

  implicit class AsepriteRectangleConvert(val obj: AsepriteRectangle) {
    def toJsDelegate =
      AsepriteRectangleDelegate(obj.x, obj.y, obj.w, obj.h)
  }

  implicit class AsepriteMetaConvert(val obj: AsepriteMeta) {
    def toJsDelegate =
      AsepriteMetaDelegate(obj.app, obj.version, obj.image, obj.format, obj.size.toJsDelegate, obj.scale, obj.frameTags.map(_.toJsDelegate))
  }

  implicit class AsepriteSizeConvert(val obj: AsepriteSize) {
    def toJsDelegate =
      AsepriteSizeDelegate(obj.w, obj.h)
  }

  implicit class AsepriteFrameTagConvert(val obj: AsepriteFrameTag) {
    def toJsDelegate =
      AsepriteFrameTagDelegate(obj.name, obj.from, obj.to, obj.direction)
  }
}

@JSExportTopLevel("SpriteAndAnimations")
final case class SpriteAndAnimationsDelegate(sprite: SpriteDelegate, animations: AnimationDelegate)
@JSExportTopLevel("AsepriteConverter")
object AsepriteDelegate {

  implicit class SpriteAndAnimationsConvert(val obj: SpriteAndAnimations) {
    def toJsDelegate =
      SpriteAndAnimationsDelegate(
        obj.sprite.toJsDelegate,
        obj.animations.toJsDelegate
      )
  }

  @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
  @JSExport
  def toSpriteAndAnimations(dice: DiceDelegate, aseprite: AsepriteDelegate, assetName: String): js.UndefOr[SpriteAndAnimationsDelegate] =
    Aseprite.toSpriteAndAnimations(aseprite.toInternal, dice.toInternal, AssetName(assetName)) match {
      case None =>
        None.orUndefined
      case Some(x) => Some(x.toJsDelegate).orUndefined
    }

  def extractCycles(aseprite: AsepriteDelegate): js.Array[CycleDelegate] =
    Aseprite.extractCycles(aseprite.toInternal).map(_.toJsDelegate).toList.toJSArray
}
