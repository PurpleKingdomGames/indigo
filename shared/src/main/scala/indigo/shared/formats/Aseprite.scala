package indigo.shared.formats

import indigo.shared.AsString

final case class Aseprite(frames: List[AsepriteFrame], meta: AsepriteMeta)

final case class AsepriteFrame(filename: String, frame: AsepriteRectangle, rotated: Boolean, trimmed: Boolean, spriteSourceSize: AsepriteRectangle, sourceSize: AsepriteSize, duration: Int)

final case class AsepriteRectangle(x: Int, y: Int, w: Int, h: Int)

final case class AsepriteMeta(app: String, version: String, image: String, format: String, size: AsepriteSize, scale: String, frameTags: List[AsepriteFrameTag])

final case class AsepriteSize(w: Int, h: Int)

final case class AsepriteFrameTag(name: String, from: Int, to: Int, direction: String)
object AsepriteFrameTag {
  implicit val show: AsString[AsepriteFrameTag] =
    AsString.create { ft =>
      s"""FrameTag(${ft.name}, ${ft.from.toString}, ${ft.to.toString}, ${ft.direction})"""
    }
}
